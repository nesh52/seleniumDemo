package com.swissbit.exense.demo.test;

import com.swissbit.exense.demo.SeleniumPricing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import step.handlers.javahandler.KeywordRunner;
import step.handlers.javahandler.KeywordRunner.ExecutionContext;

import javax.json.Json;
import java.util.HashMap;
import java.util.Map;

public class TestFoo {

    @Test
    public void test() throws Exception {

        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(SeleniumPricing.class);

//        ctx.run("testMe", Json.createObjectBuilder()
//                .add("foo", "yellow")
//                .build()
//                .toString()
//        );

        ctx.run("testMe", Json.createObjectBuilder()
//                .add("url", "step.exense.ch")
                .add("url", "step.exense.ch")
                .build()
                .toString()
        );

    }

}
