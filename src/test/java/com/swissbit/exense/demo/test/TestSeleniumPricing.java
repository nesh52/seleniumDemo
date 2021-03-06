package com.swissbit.exense.demo.test;

import com.swissbit.exense.demo.SeleniumPricing;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import step.handlers.javahandler.KeywordRunner;
import step.handlers.javahandler.KeywordRunner.ExecutionContext;

import javax.json.Json;
import java.util.ArrayList;
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
        String title = ctx.run("Go to site", Json.createObjectBuilder()
                .add("url", "https://step.exense.ch")
                .build()
                .toString()
        ).getPayload().getString("title");
        System.out.println(title);
        Assert.assertEquals("step", title);

        ctx.run("Go to pricing");

        ArrayList<String> userVolume = new ArrayList<>();
        userVolume.add("min");
        userVolume.add("low");
        userVolume.add("mid");
        userVolume.add("max");


        for (String vol : userVolume) {
            String value = ctx.run("Select premium number of users", Json.createObjectBuilder()
                    .add("volume", vol)
                    .build()
                    .toString()
            ).getPayload().getString("licenseValue");

            System.out.println("Volume: " + vol + " Value: " + value);

        }

//        ctx.run("Go to Contact");
//        ctx.run("Fill the contact form", Json.createObjectBuilder()
//                .add("firstName", "n1")
//                .add("lastName", "n2")
//                .add("email", "n3")
//                .add("message", "n4")
//                .build()
//                .toString()
//        );
//        ctx.run("Go to home using logo");
//        ctx.run("Go to contact using footer");
//        ctx.run("Fill the contact form", Json.createObjectBuilder()
//                .add("firstName", "d1")
//                .add("lastName", "d2")
//                .add("email", "d3")
//                .add("message", "d4")
//                .build()
//                .toString()
//        );
    }

    @After
    public void tearDown() {
        ctx.close();
    }
}
