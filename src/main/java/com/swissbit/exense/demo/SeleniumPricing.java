package com.swissbit.exense.demo;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

//import step.examples.selenium.SeleniumKeywordExample.DriverWrapper;
import step.grid.io.Attachment;
import step.grid.io.AttachmentHelper;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

public class SeleniumPricing extends AbstractKeyword {
    final List<String> defaultOptions = Arrays.asList(new String[]{"disable-infobars", "ignore-certificate-errors"});
    final List<String> headlessOptions = Arrays.asList(new String[]{"headless", "disable-gpu", "no-sandbox"});


    @Keyword
    public void testMe() {
        String foo = input.getString("url");

        output.add("url", foo);
    }


    @Keyword(name = "Open Chrome")
    public void openChrome() throws Exception {

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
        session.put(new DriverWrapper(driver));
    }


    @Keyword
    public void goToSite() {
        String url = input.getString("url");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.get(url);
        output.add("rethink", driver.findElement(By.xpath("//h1[text() = 'Rethink automation.']")).getText());
    }

    @Keyword(name = "Go to pricing")
    public void goToPricing() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.linkText("Pricing")).click();
        attachScreenshot(driver);
    }

    @Keyword(name = "Select premium number of users")
    public void selectPremium() {
        WebDriver driver = session.get(DriverWrapper.class).driver;

        // 1-15; 16-30; 31-60; >60
        driver.findElement(By.linkText("31-60")).click();

        String value = driver.findElement(By.xpath("//div[@class=\"tab-pane fade active show\"]/div/span[@class=\"centered-img price enterprise-premium-color\"]")).getText();

        System.out.println("value = " + value);

//		WebElement price = driver.findElement(By.xpath(
//                "//span[contains(@class,'centered-img price enterprise-premium-color')]"));
//
//		output.add(price.getText(), price.findElement(By.tagName("span")).getText());


        attachScreenshot(driver);
    }


    @Keyword(name = "Go to Contact")
    public void goToContact() throws Exception {

        WebDriver driver = session.get(DriverWrapper.class).driver;

        driver.get("https://step.exense.ch");

        //WebElement searchLink;
        driver.findElement(By.partialLinkText("ontact")).click();


        WebElement userNameElement = driver.findElement(By.id("id_first_name"));
        userNameElement.sendKeys("User");

        WebElement lastNameElement = driver.findElement(By.id("id_last_name"));
        lastNameElement.sendKeys("Usera");

        WebElement emailElement = driver.findElement(By.id("id_email"));
        emailElement.sendKeys("User@user.com");


        WebElement messageElement = driver.findElement(By.id("id_message"));
        messageElement.sendKeys("Some text goes here ....");

        attachScreenshot(driver);

        //home by logo
        driver.findElement(By.xpath("//a[@class='navbar-brand']")).click();
        attachScreenshot(driver);


    }

    @Keyword(name = "Contact from footer")
    public void contactFromFooter() throws Exception {

        WebDriver driver = session.get(DriverWrapper.class).driver;

        driver.get("https://step.exense.ch");

        driver.findElement(By.xpath("//footer[@class='footer']"))
                .findElement(By.xpath("//a[@class='btn btn-info']")).click();

        WebElement userNameElement = driver.findElement(By.id("id_first_name"));
        userNameElement.sendKeys("User");

        WebElement lastNameElement = driver.findElement(By.id("id_last_name"));
        lastNameElement.sendKeys("Usera");

        WebElement emailElement = driver.findElement(By.id("id_email"));
        emailElement.sendKeys("User@user.com");


        WebElement messageElement = driver.findElement(By.id("id_message"));
        messageElement.sendKeys("Some text goes here ....");

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

    private void setDriver(WebDriver driver) {
        this.session.put("DriverWrapper", new DriverWrapper(driver));
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
