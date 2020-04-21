package com.swissbit.exense.demo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import step.grid.io.Attachment;
import step.grid.io.AttachmentHelper;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class StepDummyPlanExecution extends AbstractKeyword {
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

        WebDriverWait wait = new WebDriverWait(driver,30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginForm")));
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Login")
    public void login() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        String username = input.getString("username");
        String password = input.getString("password");

        WebElement userEl = driver.findElement(By.name("username"));
        userEl.clear();
        WebElement passEl = driver.findElement(By.name("password"));
        passEl.clear();

        userEl.sendKeys(username);
        passEl.sendKeys(password);

        driver.findElement(By.xpath("//button[@class='btn btn-lg btn-default btn-block']")).click();

        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Create a dummy plan")
    public void createPlan() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        String planName = input.getString("planName");
        String planType = input.getString("planType");
//
// WebDriverWait wait = new WebDriverWait(driver,30);
// wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@class='btn btn-success']")));
        driver.findElement(By.xpath("//button[@class='btn btn-success']")).click();

        driver.findElement(By.id("attributes.name")).clear();
        driver.findElement(By.id("attributes.name")).sendKeys(planName);

        driver.findElement(By.xpath("//select[@class='form-control ng-pristine ng-untouched ng-valid ng-not-empty']")).sendKeys(planType);

        driver.findElement(By.xpath("//button[@class='btn btn-primary'][1]")).click();

        output.add("title", driver.getTitle());

        attachScreenshot(driver);
    }



    @Keyword(name = "Execute plan")
    public void executePlan() throws InterruptedException {
        WebDriver driver = session.get(DriverWrapper.class).driver;

        driver.findElement(By.xpath("//*[@id='ArtefactEditorCtrl']/div/div[1]/div/div[1]/div/div[2]/button")).click();
        driver.findElement((By.xpath("//*[@id='ArtefactEditorCtrl']/div/div[1]/div/div[1]/div/div[2]/div/div[2]/div/execution-commands/div/div/div/button[2]"))).click();
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Read and send id")
    public void ReadId() throws Exception {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        WebElement element =driver.findElement(By.xpath("//ul[@class='list-unstyled']/li[6]/span"));
        String id = element.getText();
        output.add("executionId", id);
        output.add("title", driver.getTitle());

        attachScreenshot(driver);
    }

    @Keyword(name = "Close tab")
    public void closeTab() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//ul[@class='nav nav-tabs']/li[@class='ng-scope active']/a/i")).click();

        String href = driver.findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/a")).getAttribute("href");
        output.add("executionId", href.substring(href.lastIndexOf("/") +1));

        output.add("title", driver.getTitle());

        attachScreenshot(driver);
    }

    @Keyword(name = "Wait for the status")
    public void waitForTheStatus() {
        WebDriver driver = session.get(DriverWrapper.class).driver;

        WebDriverWait wait = new WebDriverWait(driver,30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@role='grid']/tbody/tr[1]/td[6]/span[@class='executionStatus status-ENDED']")));

        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Errors")
    public void errors() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        String failed =driver.findElement(By.xpath("//table[@role='grid']//tr[1]/td[7]//status-distribution//div[2]")).getText();
        String techErrors =driver.findElement(By.xpath("//table[@role='grid']//tr[1]/td[7]//status-distribution//div[3]")).getText();

        output.add("Failed", failed);
        output.add("Technical errors", techErrors);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name="Logout")
    public void logout(){
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//ul[@class='nav navbar-nav navbar-right']")).click();
        driver.findElement(By.xpath("//a[@ng-click='authService.logout()']")).click();
        output.add("title", driver.getTitle());
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