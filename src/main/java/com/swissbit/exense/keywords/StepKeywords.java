package com.swissbit.exense.keywords;

import com.swissbit.exense.utils.DriverWrapper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import step.grid.io.Attachment;
import step.grid.io.AttachmentHelper;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

import java.time.Duration;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class StepKeywords extends AbstractKeyword {

    static final int EXPLICIT_WAIT_TIMEOUT_IN_SECONDS = 20;

    @Keyword(name = "Go to STEP")
    public void goToSTEP() {
        String url = input.getString("url");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.get(url);
        output.add("title", driver.getTitle());
        output.add("isLoginButtonPresent", isElementPresent(driver, By.xpath("//button[@type='submit']")));
        attachScreenshot(driver);
    }

    @Keyword(name = "Login to STEP")
    public void loginToSTEP() {
        String username = input.getString("username");
        String password = input.getString("password");
        WebDriver driver = session.get(DriverWrapper.class).driver;

        WebElement inputUsername = waitAndGetWebElement(driver, By.name("username"));
        inputUsername.clear();
        inputUsername.sendKeys(username);
        WebElement inputPassword = waitAndGetWebElement(driver, By.name("password"));
        inputPassword.clear();
        inputPassword.sendKeys(password);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        output.add("isNewPlanButtonPresent", isElementPresent(driver, By.xpath("//button[text()='New plan']")));
        attachScreenshot(driver);
    }

    @Keyword(name = "Create and edit plan")
    public void createAndEditPlan() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        waitAndGetWebElement(driver, By.xpath("//button[text()='New plan']")).click();
        output.add("isNewPlanDialogPresent", isElementPresent(driver, By.xpath("//h3[contains(@class, 'modal-title')]")));
        setPlanAttributes(driver);
        driver.findElement(By.xpath("//div[@class='modal-footer ng-scope']/button[text()='Save and edit']")).click();
        output.add("isExecutePlanButtonPresent", isElementPresent(driver, By.xpath("//button[@title='Execute this plan']")));
        attachScreenshot(driver);
    }

    private void setPlanAttributes(WebDriver driver) {
        String planName = input.getString("planName");
        String planType = input.getString("planType");
        String stepVersion = input.getString("stepVersion");

        switch (stepVersion.toLowerCase()) {
            case "v3.10.0":
                waitAndGetWebElement(driver, By.id("attributes.name")).sendKeys(planName);
                new Select(waitAndGetWebElement(driver, By.xpath("//select[@ng-model='artefacttype']"))).selectByVisibleText(planType);
                break;
            case "v3.13.0":
                waitAndGetWebElement(driver, By.xpath("//input[@ng-if=\"input.type=='TEXT'\"]")).sendKeys(planName);
                WebElement selectPlanType = waitAndGetWebElement(driver, By.xpath("//select[@ng-model='template']"));
                new Select(selectPlanType).selectByVisibleText(planType);
                break;
            default:
                output.setError("Unsupported STEP version: " + stepVersion);
        }
    }

    @Keyword(name = "Run plan")
    public void runPlan() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        waitAndGetWebElement(driver, By.xpath("//button[@title='Execute this plan']")).click();
        By executionConfirmationLocator = By.xpath("//button[@ng-click='execute(false)']");
        output.add("isExecutionConfirmationPresent", isElementPresent(driver, executionConfirmationLocator));
        waitAndGetWebElement(driver, executionConfirmationLocator).click();
        String executionId = waitAndGetWebElement(driver, By.xpath("//li[strong/text()='Execution ID']/span")).getText();
        String artifactIdRaw = waitAndGetWebElement(driver, By.xpath("//li[strong/text()='Origin']/span[@class='ng-binding ng-scope']")).getText();
        output.add("isExecutionDetailPresent", isElementPresent(driver, By.xpath("//a[text()='Execution details']")));
        output.add("artifactId", artifactIdRaw.replaceFirst("^artefactid=|^planid=", ""));
        output.add("executionId", executionId);
        attachScreenshot(driver);
    }

    @Keyword(name = "Close current execution tab")
    public void closeCurrentExecutionTab() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.xpath("//li[@class='ng-scope active']/a/i[@ng-click='closeTab(tab.id)']")).click();
        String lastExecutions = driver.findElements(By.xpath("//table[@role='grid']/tbody/tr/td//a")).stream()
                .map(e -> {
                    String href = e.getAttribute("href");
                    return href.substring(href.lastIndexOf('/') + 1);
                })
                .collect(Collectors.joining("|"));
        output.add("lastExecutions", lastExecutions);
        attachScreenshot(driver);
    }

    @Keyword(name = "Wait for execution to end")
    public void waitForExecutionToEnd() throws InterruptedException {
        int pollMaxTries = input.getInt("pollMaxTries", 30);
        int pollIntervalMilliseconds = input.getInt("pollIntervalMilliseconds", 5000);
        WebDriver driver = session.get(DriverWrapper.class).driver;
        for (int i = 1; i <= pollMaxTries; i++) {
            Thread.sleep(pollIntervalMilliseconds);
            String firstRowPath = "//table[@role='grid']/tbody/tr[1]";
            String status = driver.findElement(By.xpath(firstRowPath + "/td//span[contains(@class, 'executionStatus')]")).getText();
            if (status.equals("ENDED")) {
                String statusDistributionStr = driver.findElement(By.xpath(firstRowPath + "/td//status-distribution/div")).getAttribute("uib-tooltip");
                Pattern pattern = Pattern.compile(": (\\d+)");
                Matcher matcher = pattern.matcher(statusDistributionStr);
                ArrayList<String> results = new ArrayList<>();
                while (matcher.find()) {
                    results.add(matcher.group(1));
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
        attachScreenshot(driver);
    }

    @Keyword(name = "Go to plans")
    public void goToPlans() {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.linkText("Plans")).click();
        output.add("isNewPlanButtonPresent", isElementPresent(driver, By.xpath("//button[text()='New plan']")));
        attachScreenshot(driver);
    }

    @Keyword(name = "Remove plan by artifact id")
    public void removePlanByExecId() {
        // alternative would be to remove all created dummy plans from MongoDB
        // db.plans.remove( { "attributes.name" : "dummy1"  } )
        String artifactId = input.getString("artifactId");
        String stepVersion = input.getString("stepVersion");
        WebDriver driver = session.get(DriverWrapper.class).driver;
        removePlan(driver, artifactId, stepVersion);
        output.add("isDeleteWarningPresent", isElementPresent(driver, By.xpath("//h3[text()='Warning']")));
        confirmRemovePlan(driver);
        output.add("isRemovedPlanPresent", isArtifactIdPresent(driver, artifactId, stepVersion));
        attachScreenshot(driver);
    }

    private String isArtifactIdPresent(WebDriver driver, String artifactId, String stepVersion) {
        By artifactElement = null;
        switch (stepVersion.toLowerCase()) {
            case "v3.10.0":
                artifactElement = By.xpath("//div[@class='input-group']/div/button[@onclick=\"angular.element('#ArtefactListCtrl').scope().editArtefact('" +
                artifactId + "')\"]");
                break;
            case "v3.13.0":
                artifactElement = By.xpath("//tr/td/cell/plan-link/a[@href='#/root/plans/editor/" + artifactId + "']");
                break;
            default:
                output.setError("unsupported STEP version: " + stepVersion);
        }
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_TIMEOUT_IN_SECONDS))
                .until(ExpectedConditions.numberOfElementsToBe(artifactElement, 0));
        return String.valueOf(!driver.findElements(artifactElement).isEmpty());
    }

    private void removePlan(WebDriver driver, String artifactId, String stepVersion) {
        StringBuilder delButtonXPath = new StringBuilder();
        switch (stepVersion.toLowerCase()) {
            case "v3.10.0":
                delButtonXPath
                        .append("//button[@onclick=\"angular.element('#ArtefactListCtrl').scope().removeArtefact('")
                        .append(artifactId)
                        .append("')\"]");
                break;
            case "v3.13.0":
                delButtonXPath
                        .append("//tr[td/cell/plan-link/a[@href='#/root/plans/editor/")
                        .append(artifactId)
                        .append("']]/td//button[@uib-tooltip='Delete plan']");
                break;
            default:
                output.setError("Unsupported STEP version: " + stepVersion);
        }

        boolean staleElement = true;
        while (staleElement) {
            try {
                waitAndGetWebElement(driver, By.xpath(delButtonXPath.toString())).click();
                staleElement = false;
            } catch (StaleElementReferenceException e) {
                staleElement = true;
            } catch (Exception exception) {
                output.setError("could not recover stale element");
                output.addAttachment(AttachmentHelper.generateAttachmentForException(exception));
            }
        }
    }

    private void confirmRemovePlan(WebDriver driver) {
        try {
            waitAndGetWebElement(driver, By.xpath("//form[@name='ConfirmationDialog']/div[@class='modal-footer']/button[text()='Yes']")).click();
        } catch (Exception e) {
            output.setError("failed confirming delete plan");
            output.addAttachment(AttachmentHelper.generateAttachmentForException(e));
        }
    }

    @Keyword(name = "Logout from STEP")
    public void logoutFromSTEP() throws InterruptedException {
        WebDriver driver = session.get(DriverWrapper.class).driver;
        driver.findElement(By.id("sessionDropdown")).click();
        driver.findElement(By.xpath("//a[@ng-click='authService.logout()']")).click();
        output.add("isLoginButtonPresent", isElementPresent(driver, By.xpath("//button[@type='submit']")));
        attachScreenshot(driver);
    }

    private String isElementPresent(WebDriver driver, By by) {
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_TIMEOUT_IN_SECONDS))
                .until(ExpectedConditions.elementToBeClickable(by));
        return String.valueOf(!driver.findElements(by).isEmpty());
    }

    private void attachScreenshot(WebDriver driver) {
        if (input.getBoolean("makeScreenshot", false)) {
            try {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Attachment attachment = AttachmentHelper.generateAttachmentFromByteArray(bytes, "screenshot.jpg");
                output.addAttachment(attachment);
            } catch (Exception ex) {
                output.appendError("Unable to generate screenshot");
            }
        }
    }

    private WebElement waitAndGetWebElement(WebDriver driver, By by) {
        return waitAndGetWebElement(driver, by, EXPLICIT_WAIT_TIMEOUT_IN_SECONDS);
    }

    private WebElement waitAndGetWebElement(WebDriver driver, By by, int timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.elementToBeClickable(by));
    }

}
