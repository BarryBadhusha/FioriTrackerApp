package com.novigo.fiori.trackerapp.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.novigo.fiori.trackerapp.app.SAPWizardApplication;
import com.novigo.fiori.trackerapp.logon.LogonActivity;
import com.novigo.fiori.trackerapp.test.core.BaseTest;
import com.novigo.fiori.trackerapp.test.core.UIElements;
import com.novigo.fiori.trackerapp.test.core.Utils;
import com.novigo.fiori.trackerapp.test.core.Credentials;
import com.novigo.fiori.trackerapp.test.core.WizardDevice;
import com.novigo.fiori.trackerapp.test.pages.DetailPage;
import com.novigo.fiori.trackerapp.test.pages.PasscodePage;
import com.novigo.fiori.trackerapp.test.pages.EntityListPage;
import com.novigo.fiori.trackerapp.test.pages.MasterPage;
import com.novigo.fiori.trackerapp.test.pages.SettingsListPage;
import com.novigo.fiori.trackerapp.test.pages.WelcomePage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static com.novigo.fiori.trackerapp.test.core.UIElements.EntityListScreen.entityList;

@RunWith(AndroidJUnit4.class)
public class LogonTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);


    @Test
    public void testLogonFlow() {

        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        // Actions on the entitylist Page
        EntityListPage entityListPage = new EntityListPage(entityList);
         entityListPage.clickFirstElement();
         entityListPage.leavePage();

        // Actions on the master Page
        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();
        detailPage.leavePage();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }


    @Test
    public void logonFlowPutAppIntoBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(entityList);
         entityListPage.clickFirstElement();
         entityListPage.leavePage();

        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        // Put the app into background and immediately start again
        WizardDevice.putApplicationBackground(0, activityTestRule);
        WizardDevice.reopenApplication();

        if (lockTimeOut == 0) {
            PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
            enterPasscodePage.enterPasscode(Credentials.PASSCODE);
            enterPasscodePage.clickSignIn();
            enterPasscodePage.leavePage();
        }

        DetailPage mDetailPage = new DetailPage(UIElements.DetailScreen.deleteButton);
        mDetailPage.clickBack();
        mDetailPage.leavePage();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }
    @Test
    public void LogonFlowBack () {
        Utils.checkCredentials();
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.clickGetStarted();
        welcomePage.waitForCredentials();
        Utils.pressBack();
        Utils.doOnboarding();
        EntityListPage entityListPage = new EntityListPage(entityList);
    }
}
