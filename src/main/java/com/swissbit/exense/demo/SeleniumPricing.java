package com.swissbit.exense.demo;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import step.grid.io.Attachment;
import step.grid.io.AttachmentHelper;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SeleniumPricing extends AbstractKeyword {
    @Keyword(name = "Open Chrome")
    public void openChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList("disable-infobars"));
        boolean headless = input.getBoolean("headless", false);
        if (headless) {
            options.addArguments(Arrays.asList("headless", "disable-gpu"));
        }
        boolean sandbox = input.getBoolean("sandbox", true);
        if (!sandbox) {
            options.addArguments(Arrays.asList("no-sandbox"));
        }
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        session.put(new DriverWrapper(driver));
    }

    @Keyword(name = "Go to site")
    public void goToSite() {
        String url = input.getString("url");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.get(url);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Go to pricing")
    public void goToPricing() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.linkText("Pricing")).click();
        output.add("title", driver.getTitle());
        output.add("secHeader", getSectionHeader(driver));
        attachScreenshot(driver);
    }

    @Keyword(name = "Select premium number of users")
    public void selectPremiumNrOfUsers() throws InterruptedException {
        WebDriver driver = session.get(DriverWrapper.class).driver;
//        String nrOfUsers = input.getString("nrOfUsers"); // 1-15; 16-30; 31-60; >60
//        driver.findElement(By.linkText(nrOfUsers)).click();
        String volume = input.getString("volume");
        String userId = "pills-ep" + volume + "-tab";
        String valueDivId = "pills-ep" + volume;
        driver.findElement(By.id(userId)).click();
        Thread.sleep(1_000);
        String value = driver.findElement(By.xpath("//div[@id='" + valueDivId + "']/div/span[@class=\"centered-img price enterprise-premium-color\"]")).getText();
        output.add("licenseValue", value);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Go to Contact")
    public void goToContact() throws Exception {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.partialLinkText("ontact")).click();
        output.add("title", driver.getTitle());
        output.add("secHeader", getSectionHeader(driver));
        attachScreenshot(driver);
    }

    @Keyword(name = "Fill the contact form")
    public void fillContactForm() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        String firstName = input.getString("firstName");
        String lastName = input.getString("lastName");
        String email = input.getString("email");
        String message = input.getString("message");

        driver.findElement(By.id("id_first_name")).sendKeys(firstName);
        driver.findElement(By.id("id_last_name")).sendKeys(lastName);
        driver.findElement(By.id("id_email")).sendKeys(email);
        driver.findElement(By.id("id_message")).sendKeys(message);
        output.add("title", driver.getTitle());
    }

    @Keyword(name = "Go to home using logo")
    public void goToHomeUsingLogo() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//a[@class='navbar-brand']")).click();
        output.add("rethink", driver.findElement(By.xpath("//h1[text() = 'Rethink automation.']")).getText());
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Go to contact using footer")
    public void goToContactUsingFooter() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//footer[@class='footer']")).findElement(By.xpath("//a[@class='btn btn-info']")).click();
        output.add("title", driver.getTitle());
        output.add("secHeader", getSectionHeader(driver));
        attachScreenshot(driver);
    }

    public void attachScreenshot(WebDriver driver) {
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Attachment attachment = AttachmentHelper.generateAttachmentFromByteArray(bytes, "screenshot.jpg");
            output.addAttachment(attachment);
        } catch (Exception ex) {
            output.appendError("Unable to generate screenshot");
        }
    }

    private String getSectionHeader(WebDriver driver) {
        return driver.findElement(By.xpath("//section[@class='middle']/header/h2")).getText();
    }


    public class DriverWrapper implements Closeable {
        final WebDriver driver;
        public DriverWrapper(WebDriver driver) {
            super();
            this.driver = driver;
        }
        @Override
        public void close() throws IOException {
            driver.quit();
        }
    }
}
