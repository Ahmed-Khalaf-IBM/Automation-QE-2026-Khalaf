package com.framework.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Parent of every page object: shared, reusable UI actions. */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void click(Locator l) {
        l.click();
    }

    protected void type(Locator l, String text) {
        l.fill(text);
    }

    protected String text(Locator l) {
        return l.innerText().trim();
    }

    protected boolean isVisible(Locator l) {
        return l.isVisible();
    }

    public String currentUrl() {
        return page.url();
    }
}
