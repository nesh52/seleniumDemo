package com.swissbit.exense.demo.test;

import com.swissbit.exense.keywords.ChromeDriverKeywords;
import com.swissbit.exense.keywords.StepKeywords;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import step.handlers.javahandler.KeywordRunner;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TestStepPlanExecution {

    private static KeywordRunner.ExecutionContext ctx;

    @BeforeAll
    public static void setUp() {
        Map<String, String> properties = new HashMap<>();
        ctx = KeywordRunner.getExecutionContext(properties, StepKeywords.class, ChromeDriverKeywords.class);
    }

    @ParameterizedTest
    @MethodSource("provideStepVersions")
    void runTest(String stepVersion, String stepUrl, String username, String password) throws Exception {

        // ----- Open Chrome ------
        ctx.run("Open Chrome", Json.createObjectBuilder()
//                .add("proxyHost", "127.0.0.1")
//                .add("proxyPort", 8888)
                        .build()
                        .toString()
        );

        // ----- Go to STEP ------
        String lendingTitle = ctx.run("Go to STEP", Json.createObjectBuilder()
                .add("url", stepUrl)
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", lendingTitle);

        // ----- Login to STEP ------
        String loginTitle = ctx.run("Login to STEP", Json.createObjectBuilder()
                .add("username", username)
                .add("password", password)
                .build()
                .toString()
        ).getPayload().getString("name");
        Assert.assertEquals("Login", loginTitle);

        // ----- Create and edit STEP plan ------
        String titleAfterCreate = ctx.run("Create and edit plan", Json.createObjectBuilder()
                .add("planName", "dummy1")
                .add("planType", "Sequence") // "Sequence", "TestScenario", "Echo"
                .add("stepVersion", stepVersion)
                .build()
                .toString()
        ).getPayload().getString("plan");
        Assert.assertEquals("Plans", titleAfterCreate);

        // ----- Run plan ------
        JsonObject planDetails = ctx.run("Run plan", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        String executionId = planDetails.getString("executionId");
        String artifactId = planDetails.getString("artifactId");
        String exec = planDetails.getString("exec");
        Assert.assertEquals("Executions", exec);
        System.out.println("executionId: " + executionId);
        System.out.println("artifactId: " + artifactId);

        // ----- Close current execution tab ------
        String lastExecId = ctx.run("Close current execution tab", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("lastExecutionId");
        System.out.println("lastExecId: " + lastExecId);
        Assert.assertEquals(executionId, lastExecId);

        // ----- Wait for execution to end ------
        JsonObject execStatus = ctx.run("Wait for execution to end", Json.createObjectBuilder()
                .add("pollMaxTries", 300)
                .add("pollIntervalMilliseconds", 2000)
                .build()
                .toString()
        ).getPayload();
        String pass = execStatus.getString("passed");
        String fail = execStatus.getString("failed");
        String error = execStatus.getString("technicalError");
        String tab = execStatus.getString("tab");
        System.out.println("pass: " + pass);
        System.out.println("fail: " + fail);
        System.out.println("error: " + error);
        Assert.assertEquals("0", pass);
        Assert.assertEquals("0", fail);
        Assert.assertEquals("0", error);
        Assert.assertEquals("Execution list", tab);

        // ----- Go to plans ------
        JsonObject plansGrid = ctx.run("Go to plans", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        String plan = plansGrid.getString("plan");

        Assert.assertEquals("Plans", plan);

        // ----- Remove plan by artifact id ------
       JsonObject remove = ctx.run("Remove plan by artifact id", Json.createObjectBuilder()
                .add("artifactId", artifactId)
                .add("stepVersion", stepVersion)
                .build()
                .toString()
        ).getPayload();
       String keywords = remove.getString("keywords");
        Assert.assertEquals("Keywords", keywords);

        // ----- Logout from STEP ------
        String logoutTitle = ctx.run("Logout from STEP", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("logout");
        Assert.assertEquals("Login", logoutTitle);
    }

    private static Stream<Arguments> provideStepVersions() {
        return Stream.of(
                Arguments.of("v3.10.0", "https://step-public-demo.stepcloud.ch/", "admin", "public"),
                Arguments.of("v3.13.0", "http://localhost:8080", "admin", "init")
        );
    }

    @AfterAll
    public static void tearDown() {
        ctx.close();
    }

}
