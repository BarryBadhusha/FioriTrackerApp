package com.novigo.fiori.trackerapp.test.testcases.ui;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import static androidx.test.InstrumentationRegistry.getInstrumentation;

import com.novigo.fiori.trackerapp.R;
import com.novigo.fiori.trackerapp.app.SAPWizardApplication;
import com.novigo.fiori.trackerapp.logon.LogonActivity;
import com.novigo.fiori.trackerapp.test.core.BaseTest;
import com.novigo.fiori.trackerapp.test.core.Utils;
import com.novigo.fiori.trackerapp.test.pages.EntityListPage;
import com.novigo.fiori.trackerapp.test.pages.SettingsListPage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import ch.qos.logback.classic.Level;

@RunWith(AndroidJUnit4.class)
public class LoggingTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);

    private static final String DEFAULT_LOG_LEVEL = "Warn";

    @Test
    public void testLogPolicyValue() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage();
        entityListPage.clickSettings();
        entityListPage.leavePage();

        // Check log level value
        Level policyLevel = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getClientPolicyManager().getClientPolicy(true).getLogLevel();
        String policyString;
        // If the policyLevel is null it means there is no policy set on the server
        if (policyLevel == null) {
            policyString = DEFAULT_LOG_LEVEL;
        } else {
            policyString = (policyLevel.levelStr.toLowerCase());
            policyString = policyString.substring(0, 1).toUpperCase() + policyString.substring(1);
            if (policyString.equals("All")) {
                policyString = "Path";
            }
            if (policyString.equals("Off")) {
                policyString = "None";
            }
        }
        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.checkLoglevel(policyString);
    }

}
