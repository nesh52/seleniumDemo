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
        String isLoginButtonPresent = ctx.run("Go to STEP", Json.createObjectBuilder()
                .add("url", stepUrl)
                .build()
                .toString()
        ).getPayload().getString("isLoginButtonPresent");
        Assert.assertTrue(Boolean.valueOf(isLoginButtonPresent));

        // ----- Login to STEP ------
        String isNewPlanButtonPresent = ctx.run("Login to STEP", Json.createObjectBuilder()
                .add("username", username)
                .add("password", password)
                .build()
                .toString()
        ).getPayload().getString("isNewPlanButtonPresent");
        Assert.assertTrue(Boolean.valueOf(isNewPlanButtonPresent));

        // ----- Create and edit STEP plan ------
        JsonObject createAndEditPayload = ctx.run("Create and edit plan", Json.createObjectBuilder()
                .add("planName", "dummy1")
                .add("planType", "Sequence") // "Sequence", "TestScenario", "Echo"
                .add("stepVersion", stepVersion)
                .build()
                .toString()
        ).getPayload();
        Assert.assertTrue(Boolean.valueOf(createAndEditPayload.getString("isNewPlanDialogPresent")));
        Assert.assertTrue(Boolean.valueOf(createAndEditPayload.getString("isExecutePlanButtonPresent")));

        // ----- Run plan ------
        JsonObject planDetails = ctx.run("Run plan", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        Assert.assertTrue(Boolean.valueOf(planDetails.getString("isExecutionConfirmationPresent")));
        Assert.assertTrue(Boolean.valueOf(planDetails.getString("isExecutionDetailPresent")));
        String executionId = planDetails.getString("executionId");
        String artifactId = planDetails.getString("artifactId");
        System.out.println("executionId: " + executionId);
        System.out.println("artifactId: " + artifactId);

        // ----- Close current execution tab ------
        JsonObject lastExec = ctx.run("Close current execution tab", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        String lastExecutions = lastExec.getString("lastExecutions");
        System.out.println("lastExecutions: " + lastExecutions);
        Assert.assertTrue(lastExecutions.contains(executionId));

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
        System.out.println("pass: " + pass);
        System.out.println("fail: " + fail);
        System.out.println("error: " + error);
        Assert.assertEquals("0", pass);
        Assert.assertEquals("0", fail);
        Assert.assertEquals("0", error);

        // ----- Go to plans ------
        String isNewPlanButtonPresentHome = ctx.run("Go to plans", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("isNewPlanButtonPresent");
        Assert.assertTrue(Boolean.valueOf(isNewPlanButtonPresentHome));

        // ----- Remove plan by artifact id ------
       JsonObject removePlanPayload = ctx.run("Remove plan by artifact id", Json.createObjectBuilder()
                .add("artifactId", artifactId)
                .add("stepVersion", stepVersion)
                .build()
                .toString()
        ).getPayload();
       Assert.assertTrue(Boolean.valueOf(removePlanPayload.getString("isDeleteWarningPresent")));
       Assert.assertFalse(Boolean.valueOf(removePlanPayload.getString("isRemovedPlanPresent")));

        // ----- Logout from STEP ------
        String isLoginButtonPresentLogout = ctx.run("Logout from STEP", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("isLoginButtonPresent");
        Assert.assertTrue(Boolean.valueOf(isLoginButtonPresentLogout));
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
