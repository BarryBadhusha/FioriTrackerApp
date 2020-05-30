/*
 *  Copyright 2018 SAP Inc.
 *
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * Changes made by SAP
 * 1. Package name
 * 2. Change logger from android.util.logger to SAP Android SDK logger implementation
 * 3. Naming follows Google Java style instead of the use of Hungarian notation
 * 4. UIThread instead of MainThread notation
 * 5. Use lambda for super.observe
 */

package com.novigo.fiori.trackerapp.archcomp;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.UiThread;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 * <p>
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 * <p>
 * Note that only one observer is going to be notified of changes.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger("SingleLiveEvent");

    private final AtomicBoolean pending = new AtomicBoolean(false);

    @UiThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (hasActiveObservers()) {
            LOGGER.warn("Multiple observers registered but only one will be notified of changes.");
        }
        super.observe(owner, (t)-> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @UiThread
    public void setValue(@Nullable T t) {
        pending.set(true);
        super.setValue(t);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @UiThread
    public void call() {
        setValue(null);
    }
}