package com.framework.ui;

import com.framework.core.DriverManager;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseUiTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverManager.start();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            Path shot = Paths.get("test-results", "screenshots", result.getName() + ".png");
            DriverManager.page().screenshot(new Page.ScreenshotOptions().setPath(shot).setFullPage(true));
        }
        DriverManager.stop();
    }

    protected Page page() {
        return DriverManager.page();
    }
}
