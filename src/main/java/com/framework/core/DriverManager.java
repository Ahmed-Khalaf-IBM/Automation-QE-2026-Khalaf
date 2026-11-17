package com.framework.core;

import com.framework.config.ConfigManager;
import com.microsoft.playwright.*;

/**
 * Holds one Playwright/Browser/Context/Page per thread (ThreadLocal) so tests can run in parallel safely.
 */
public final class DriverManager {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private DriverManager() {}

    public static void start() {
        ConfigManager cfg = ConfigManager.get();
        Playwright pw = Playwright.create();
        Browser browser = BrowserFactory.create(pw, cfg.get("browser"), cfg.getBool("headless"), cfg.getInt("slowmo.ms"));
        BrowserContext ctx = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900));
        ctx.setDefaultTimeout(cfg.getInt("default.timeout.ms"));
        PLAYWRIGHT.set(pw);
        BROWSER.set(browser);
        CONTEXT.set(ctx);
        PAGE.set(ctx.newPage());
    }

    public static Page page() {
        if (PAGE.get() == null) throw new IllegalStateException("Browser not started. Call DriverManager.start()");
        return PAGE.get();
    }

    public static void stop() {
        if (CONTEXT.get() != null) CONTEXT.get().close();
        if (BROWSER.get() != null) BROWSER.get().close();
        if (PLAYWRIGHT.get() != null) PLAYWRIGHT.get().close();
        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
    }
}
