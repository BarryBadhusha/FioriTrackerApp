package com.novigo.fiori.trackerapp.mdui;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.annotation.Nullable;

import com.novigo.fiori.trackerapp.R;
import com.novigo.fiori.trackerapp.app.SAPWizardApplication;
import com.novigo.fiori.trackerapp.logon.ClientPolicyManager;
import com.novigo.fiori.trackerapp.logon.SecureStoreManager;

import com.sap.cloud.mobile.onboarding.passcode.ChangePasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings;
import com.sap.cloud.mobile.onboarding.passcode.EnterPasscodeSettings;

import androidx.preference.ListPreference;
import ch.qos.logback.classic.Level;
import com.novigo.fiori.trackerapp.app.LogUtil;
import com.sap.cloud.mobile.foundation.logging.Logging;
import androidx.annotation.NonNull;

import android.widget.Toast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.novigo.fiori.trackerapp.app.ErrorMessage;
import com.novigo.fiori.trackerapp.app.ErrorHandler;

/** This fragment represents the settings screen. */
public class SettingsFragment extends PreferenceFragmentCompat implements ClientPolicyManager.LogLevelChangeListener, Logging.UploadListener {

    private SAPWizardApplication sapWizardApplication;
    private SecureStoreManager secureStoreManager;
    private ClientPolicyManager clientPolicyManager;
    private ListPreference logLevelPreference;
    private ErrorHandler errorHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsFragment.class);

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        sapWizardApplication = ((SAPWizardApplication) getActivity().getApplication());
        secureStoreManager = sapWizardApplication.getSecureStoreManager();
        clientPolicyManager = sapWizardApplication.getClientPolicyManager();
        errorHandler = sapWizardApplication.getErrorHandler();

        addPreferencesFromResource(R.xml.preferences);

        LogUtil logUtil = sapWizardApplication.getLogUtil();
        logLevelPreference = (ListPreference) findPreference(getString(R.string.log_level));

        // IMPORTANT - This is where set entries...
        logLevelPreference.setEntries(logUtil.getLevelStrings());
        logLevelPreference.setEntryValues(logUtil.getLevelValues());
        logLevelPreference.setPersistent(true);

        Level logLevelStored = secureStoreManager.getWithPasscodePolicyStore(
                passcodePolicyStore -> passcodePolicyStore.getSerializable(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL)
        );
        logLevelPreference.setSummary(logUtil.getLevelString(logLevelStored));
        logLevelPreference.setValue(String.valueOf(logLevelStored.levelInt));
        logLevelPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            // Get the new value
            Level logLevel = Level.toLevel(Integer.valueOf((String) newValue));
            
            // Write the new value to the SecureStore
            secureStoreManager.doWithPasscodePolicyStore(passcodePolicyStore -> {
                passcodePolicyStore.put(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL, logLevel);
            });

            // Initialize logging
            Logging.getRootLogger().setLevel(logLevel);
            preference.setSummary(logUtil.getLevelString(logLevel));

            return true;
        });
        clientPolicyManager.setLogLevelChangeListener(this);

        // Upload log
        final Preference logUploadPreference = findPreference(getString(R.string.upload_log));
        logUploadPreference.setOnPreferenceClickListener((preference) -> {
            logUploadPreference.setEnabled(false);
            Logging.upload();
            return false;
        });

        Preference changePasscodePreference = findPreference(getString(R.string.manage_passcode));
        if (secureStoreManager.isPasscodePolicyEnabled()) {
            changePasscodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent;
                    if (secureStoreManager.isUserPasscodeSet()) {
                        intent = new Intent(SettingsFragment.this.getActivity(), ChangePasscodeActivity.class);
                        SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                        setPasscodeSettings.setSkipButtonText(getString(R.string.skip_passcode));
                        setPasscodeSettings.saveToIntent(intent);
                        int currentRetryCount = secureStoreManager.getWithPasscodePolicyStore(
                                passcodePolicyStore -> passcodePolicyStore.getInt(ClientPolicyManager.KEY_RETRY_COUNT)
                        );
                        int retryLimit = clientPolicyManager.getClientPolicy(false).getPasscodePolicy().getRetryLimit();
                        if (retryLimit <= currentRetryCount) {
                            EnterPasscodeSettings enterPasscodeSettings = new EnterPasscodeSettings();
                            enterPasscodeSettings.setFinalDisabled(true);
                            enterPasscodeSettings.saveToIntent(intent);
                        }
                        SettingsFragment.this.getActivity().startActivity(intent);
                    } else {
                        intent = new Intent(SettingsFragment.this.getActivity(), SetPasscodeActivity.class);
                        SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                        setPasscodeSettings.setSkipButtonText(getString(R.string.skip_passcode));
                        setPasscodeSettings.saveToIntent(intent);
                        SettingsFragment.this.getActivity().startActivity(intent);
                    }
                    return false;
                }
            });
        } else {
            changePasscodePreference.setEnabled(false);
            getPreferenceScreen().removePreference(changePasscodePreference);
        }

        // Reset App
        Preference resetAppPreference = findPreference(getString(R.string.reset_app));
        resetAppPreference.setOnPreferenceClickListener((preference) -> {
            sapWizardApplication.resetAppWithUserConfirmation();
            return false;
        });
    }

    @Override
    public void onDestroy() {
        clientPolicyManager.removeLogLevelChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void logLevelChanged(Level newLogLevel) {
        logLevelPreference.callChangeListener(Integer.toString(newLogLevel.levelInt));
    }

    @Override
    public void onResume() {
        super.onResume();
        Logging.addLogUploadListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logging.removeLogUploadListener(this);
    }

    @Override
    public void onSuccess() {
        enableLogUploadButton();
        Toast.makeText(getActivity(), R.string.log_upload_ok, Toast.LENGTH_LONG).show();
        LOGGER.info("Log is uploaded to the server.");
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        enableLogUploadButton();
        String errorCause = throwable.getLocalizedMessage();
        ErrorMessage errorMessage = new ErrorMessage(getString(R.string.log_upload_failed), errorCause, new Exception(throwable), false);
        errorHandler.sendErrorMessage(errorMessage);
        LOGGER.error("Log upload failed with error message: " + errorCause);
    }

    @Override
    public void onProgress(int i) {
        // You could add a progress indicator and update it from here
    }

    private void enableLogUploadButton() {
        final Preference logUploadPreference = findPreference(getString(R.string.upload_log));
        logUploadPreference.setEnabled(true);
    }
}
