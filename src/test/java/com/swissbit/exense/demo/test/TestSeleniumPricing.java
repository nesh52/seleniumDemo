package com.swissbit.exense.demo.test;

import com.swissbit.exense.demo.SeleniumPricing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import step.functions.io.Output;
import step.handlers.javahandler.KeywordRunner;
import step.handlers.javahandler.KeywordRunner.ExecutionContext;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class TestSeleniumPricing {

    private ExecutionContext ctx;

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<>();
        ctx = KeywordRunner.getExecutionContext(properties, SeleniumPricing.class);
    }

    @Test
    public void test() throws Exception {
        ctx.run("Open Chrome");
        ctx.run("goToSite", Json.createObjectBuilder()
                .add("url", "https://step.exense.ch")
                .build()
                .toString()
        );
//        ctx.run("Go to site", "{\"url\":\"step.exense.ch\"}");
//        ctx.run("Go to pricing");
//        ctx.run("Select premium number of users");
//        ctx.run("Contact from footer");
    }

    @After
    public void tearDown() {
        ctx.close();
    }


}
