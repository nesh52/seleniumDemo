package com.swissbit.exense.keywords;

import com.swissbit.exense.utils.DriverWrapper;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ChromeDriverKeywords extends AbstractKeyword {

    @Keyword(name = "Open Chrome")
    public void openChrome() {
        ChromeOptions options = new ChromeOptions();
        setDefaultOptions(options);
        setHeadless(options);
        setSandbox(options);
        setProxy(options);
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        session.put(new DriverWrapper(driver));
    }

    private void setDefaultOptions(ChromeOptions options) {
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments(Arrays.asList("disable-infobars", "start-maximized"));
    }

    private void setProxy(ChromeOptions options) {
        if (input.containsKey("proxyHost") && input.containsKey("proxyPort")) {
            String proxyHost = input.getString("proxyHost");
            int proxyPort = input.getInt("proxyPort");
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyHost + ":" + proxyPort);
            options.setCapability("proxy", proxy);
        }
    }

    private void setSandbox(ChromeOptions options) {
        boolean sandbox = input.getBoolean("sandbox", true);
        if (!sandbox) {
            options.addArguments(Arrays.asList("no-sandbox"));
        }
    }

    private void setHeadless(ChromeOptions options) {
        boolean headless = input.getBoolean("headless", false);
        if (headless) {
            options.addArguments(Arrays.asList("headless", "disable-gpu"));
        }
    }
}
