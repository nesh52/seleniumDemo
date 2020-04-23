package com.swissbit.exense.utils;

import org.openqa.selenium.chrome.ChromeDriver;

import java.io.Closeable;

public class DriverWrapper implements Closeable {
    public final ChromeDriver driver;

    public DriverWrapper(ChromeDriver driver) {
        super();
        this.driver = driver;
    }

    @Override
    public void close() {
        driver.quit();
    }
}