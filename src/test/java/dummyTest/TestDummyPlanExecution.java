package dummyTest;

import dummyPlan.DummyPlanExecution;
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
        ctx.run("Open Chrome v13");

        String lendingTitle = ctx.run("Go to STEP v13", Json.createObjectBuilder()
                .add("url", "http://localhost:8080")
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", lendingTitle);

//        String loginTitle = ctx.run("Login to STEP", Json.createObjectBuilder()
//                .add("username", "admin")
//                .add("password", "public")
//                .build()
//                .toString()
//        ).getPayload().getString("title");
//        Assert.assertEquals("STEP", loginTitle);

        String titleAfterCreate = ctx.run("Create plan v13", Json.createObjectBuilder()
                .add("planName", "dummy1")
                .add("planType", "Sequence") // "Sequence", "TestScenario", "Echo"
                .build()
                .toString()
        ).getPayload().getString("title");
        Assert.assertEquals("STEP", titleAfterCreate);

        JsonObject planDetails = ctx.run("Run plan v13", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        String executionId = planDetails.getString("executionId");
        String artifactId = planDetails.getString("artifactId");
        System.out.println("executionId: " + executionId);
        System.out.println("artifactId: " + artifactId);

        String lastExecId = ctx.run("Close current execution tab v13", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload().getString("lastExecutionId");
        System.out.println("lastExecId: " + lastExecId);
        Assert.assertEquals(executionId, lastExecId);

        JsonObject execStatus = ctx.run("Wait for execution to end v13", Json.createObjectBuilder()
                .add("pollMaxTries", 300)
                .add("pollIntervalMilliseconds", 2000)
                .build()
                .toString()
        ).getPayload();
        int pass = execStatus.getInt("passed");
        int fail = execStatus.getInt("failed");
        int error = execStatus.getInt("technicalError");
        System.out.println("pass: " + pass);
        System.out.println("fail: " + fail);
        System.out.println("error: " + error);
        Assert.assertEquals(0, pass);
        Assert.assertEquals(0, fail);
        Assert.assertEquals(0, error);

        JsonObject plansGrid = ctx.run("Go to plans v13", Json.createObjectBuilder()
                .build()
                .toString()
        ).getPayload();
        Assert.assertEquals("STEP", plansGrid.getString("title"));
        System.out.println("list of plan names: " + plansGrid.getString("plans"));

        String plansAfterRemove = ctx.run("Remove plan by artifact id v13", Json.createObjectBuilder()
                .add("artifactId", artifactId)
                .build()
                .toString()
        ).getPayload().getString("plans");
        System.out.println("plan names after remove: " + plansAfterRemove);

//        String logoutTitle = ctx.run("Logout from STEP", Json.createObjectBuilder()
//                .build()
//                .toString()
//        ).getPayload().getString("title");
//        Assert.assertEquals("STEP", logoutTitle);
    }

    @After
    public void tearDown() {
        ctx.close();
    }
}
