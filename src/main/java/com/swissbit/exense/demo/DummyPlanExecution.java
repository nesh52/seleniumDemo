package com.swissbit.exense.demo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import step.grid.io.Attachment;
import step.grid.io.AttachmentHelper;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DummyPlanExecution extends AbstractKeyword {

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

    @Keyword(name = "Go to STEP")
    public void goToSTEP() {
        String url = input.getString("url");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.get(url);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Login to STEP")
    public void loginToSTEP() {
        String username = input.getString("username");
        String password = input.getString("password");
        WebDriver driver = session.get(DriverWrapper.class).driver;

        WebElement inputUsername = driver.findElement(By.name("username"));
        WebElement inputPassword = driver.findElement(By.name("password"));
        inputUsername.clear();
        inputUsername.sendKeys(username);
        inputPassword.clear();
        inputPassword.sendKeys(password);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Create plan")
    public void createPlan() {
        String planName = input.getString("planName");
        String planType = input.getString("planType");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//button[text()='New plan']")).click();
        driver.findElement(By.id("attributes.name")).sendKeys(planName);
        Select newPlanTypeDropdowns = new Select(driver.findElement(By.xpath("//select[@ng-model='artefacttype']")));
        newPlanTypeDropdowns.selectByVisibleText(planType);
        driver.findElement(By.xpath("//div[@class='modal-footer ng-scope']/button[text()='Save and edit']")).click();

        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Run plan")
    public void runPlan() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//button[@title='Execute this plan']")).click();
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button/span[@class='glyphicon glyphicon glyphicon-play']")));
        driver.findElement(By.xpath("//button/span[@class='glyphicon glyphicon glyphicon-play']")).click();
        String executionId = driver.findElement(By.xpath("//li[strong/text()='Execution ID']/span")).getText();
        String artifactIdRaw = driver.findElement(By.xpath("//li[strong/text()='Origin']/span[@class='ng-binding ng-scope']")).getText();
        output.add("artifactId", artifactIdRaw.replaceFirst("^artefactid=", ""));
        output.add("executionId", executionId);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Close current execution tab")
    public void closeCurrentExecutionTab() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//li[@class='ng-scope active']/a/i[@ng-click='closeTab(tab.id)']")).click();
        String lastExecutionHref = driver.findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td/a")).getAttribute("href");
        output.add("lastExecutionId", lastExecutionHref.substring(lastExecutionHref.lastIndexOf('/') + 1));
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Wait for execution to end")
    public void waitForExecutionToEnd() throws InterruptedException {
        int pollMaxTries = input.getInt("pollMaxTries", 30);
        int pollIntervalMilliseconds = input.getInt("pollIntervalMilliseconds", 5000);
        WebDriver driver = session.get(DriverWrapper.class).driver;
        for (int i = 1; i <= pollMaxTries; i++) {
            Thread.sleep(pollIntervalMilliseconds);
            WebElement lastExecution = driver.findElement(By.xpath("//table[@role='grid']/tbody/tr[1]"));
            String status = lastExecution.findElement(By.xpath("./td/span[contains(@class, 'executionStatus')]")).getText();
            if (status.equals("ENDED")) {
                String statusDistributionStr = lastExecution.findElement(By.xpath("./td//status-distribution/div")).getAttribute("uib-tooltip");
                Pattern pattern = Pattern.compile(": (\\d+)");
                Matcher matcher = pattern.matcher(statusDistributionStr);
                ArrayList<Integer> results = new ArrayList<>();
                while (matcher.find()) {
                    results.add(Integer.valueOf(matcher.group(1)));
                }
                output.add("passed", results.get(0));
                output.add("failed", results.get(1));
                output.add("technicalError", results.get(2));
                break;
            }
            if (i == 30) {
                output.setBusinessError("execution is not finished after polling " + pollMaxTries + " times and interval " + pollIntervalMilliseconds + " milliseconds");
            }
        }
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Go to plans")
    public void goToPlans() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//a[@ng-click=\"setView('artefacts')\"]")).click();

        String plans = getPlans(driver);
        output.add("plans", plans);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }


    @Keyword(name = "Remove plan by artifact id")
    public void removePlanByExecId() {
        String artifactId = input.getString("artifactId");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        String delButtonXPath = new StringBuilder()
                .append("//button[@onclick=\"angular.element('#ArtefactListCtrl').scope().removeArtefact('")
                .append(artifactId)
                .append("')\"]")
                .toString();

        driver.findElement(By.xpath(delButtonXPath)).click();
        driver.findElement(By.xpath("//form[@name='ConfirmationDialog']/div[@class='modal-footer']/button[text()='Yes']")).click();
        driver.navigate().refresh();

        String plans = getPlans(driver);
        output.add("plans", plans);
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    @Keyword(name = "Logout from STEP")
    public void logoutFromSTEP() throws InterruptedException {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.id("sessionDropdown")).click();
        driver.findElement(By.xpath("//a[@ng-click='authService.logout()']")).click();
        output.add("title", driver.getTitle());
        attachScreenshot(driver);
    }

    private String getPlans(WebDriver driver) {
        return driver.findElements(By.xpath("//table[@role='grid']/tbody/tr/td[@class='sorting_1']"))
                .stream()
                .map(e -> e.getText())
                .collect(Collectors.joining("#"));
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
