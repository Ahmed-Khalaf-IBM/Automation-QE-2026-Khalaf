package com.framework.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * DESIGN PATTERN: Factory.
 * Creates the right Browser from a name (chromium | firefox | webkit) so tests never call "new" on a browser.
 */
public final class BrowserFactory {

    private BrowserFactory() {}

    public static Browser create(Playwright playwright, String name, boolean headless, int slowMo) {
        BrowserType type = switch (name.toLowerCase()) {
            case "chromium", "chrome" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit", "safari" -> playwright.webkit();
            default -> throw new IllegalArgumentException("Unsupported browser: " + name);
        };
        return type.launch(new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(slowMo));
    }
}
