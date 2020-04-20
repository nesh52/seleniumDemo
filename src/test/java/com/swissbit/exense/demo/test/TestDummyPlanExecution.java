package com.swissbit.exense.demo.test;

import com.swissbit.exense.demo.DummyPlanExecution;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import step.handlers.javahandler.KeywordRunner;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class TestDummyPlanExecution {

    private KeywordRunner.ExecutionContext ctx;

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<>();
        ctx = KeywordRunner.getExecutionContext(properties, DummyPlanExecution.class);
    }

    @Test
    public void test() throws Exception {
        ctx.run("Open Chrome");

        String lendingTitle = ctx.run("Go to STEP", Json.createObjectBuilder()
                .add("url", "https://step-public-demo.stepcloud.ch")
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", lendingTitle);

        String loginTitle = ctx.run("Login to STEP", Json.createObjectBuilder()
                .add("username", "admin")
                .add("password", "public")
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", loginTitle);

        String titleAfterCreate = ctx.run("Create plan", Json.createObjectBuilder()
                .add("planName", "dummy1")
//                .add("planType", "TestScenario")
                .add("planType", "Sequence")
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", titleAfterCreate);

        JsonObject planDetails = ctx.run("Run plan", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        String executionId = planDetails.getString("executionId");
        String artifactId = planDetails.getString("artifactId");
        System.out.println("executionId: " + executionId);
        System.out.println("artifactId: " + artifactId);

        String lastExecId = ctx.run("Close current execution tab", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("lastExecutionId");
        System.out.println("lastExecId: " + lastExecId);
        Assert.assertEquals(executionId, lastExecId);


        JsonObject execStatus = ctx.run("Wait for execution to end", Json.createObjectBuilder()
                .add("pollMaxTries", 30)
                .add("pollIntervalMilliseconds", 5000)
                .build()
                .toString()
        ).getPayload();
        String pass = execStatus.getString("passed");
        String fail = execStatus.getString("failed");
        String error = execStatus.getString("technicalError");
        System.out.println("pass: " + pass);
        System.out.println("fail: " + fail);
        System.out.println("error: " + error);

        String titleAfterPlans = ctx.run("Go to plans", Json.createObjectBuilder()
                        .build()
                        .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", titleAfterPlans);

        String plansAfterRemove = ctx.run("Remove plan by artifact id", Json.createObjectBuilder()
                        .add("artifactId", artifactId)
                        .build()
                        .toString()
        ).getPayload().getString("plans");
        System.out.println(plansAfterRemove);

        String logoutTitle = ctx.run("Logout from STEP", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", logoutTitle);
    }

    @After
    public void tearDown() {
        ctx.close();
    }
}
