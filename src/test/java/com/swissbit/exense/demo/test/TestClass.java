package com.swissbit.exense.demo.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.Response;

public class TestClass {

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    void isBlank_ShouldReturnTrueForNullOrBlankStrings(int downloadThroughput, int uploadThroughput) throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);


        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(30L, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(3L, TimeUnit.SECONDS);

        if (downloadThroughput > 0 && uploadThroughput > 0) {
            CommandExecutor executor = driver.getCommandExecutor();

            Map map = new HashMap();
            map.put("offline", false);
            map.put("latency", 5);

            map.put("download_throughput", downloadThroughput);
            map.put("upload_throughput", uploadThroughput);
            Response response = executor.execute(
                    new Command(driver.getSessionId(),
                            "setNetworkConditions",
                            ImmutableMap.of("network_conditions", ImmutableMap.copyOf(map))));
        }


        driver.get("http://google.com");

        driver.quit();
    }

    private static Stream<Arguments> provideStringsForIsBlank() {
        return Stream.of(
                Arguments.of(1, 1)
//                Arguments.of(5_000, 5_000),
//                Arguments.of(10_000, 7_000),
//                Arguments.of(15_000, 9_000),
//                Arguments.of(20_000, 10_000),
//                Arguments.of(23_000, 11_000),
//                Arguments.of(30_000, 15_000),
//                Arguments.of(40_000, 20_000),
//                Arguments.of(50_000, 20_000),
//                Arguments.of(75_000, 20_000),
//                Arguments.of(100_000, 20_000)
        );
    }

}