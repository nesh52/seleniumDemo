package com.swissbit.exense.demo;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CommandExecutor;
import step.handlers.javahandler.Keyword;

public class NetworkSimulator {


    @Keyword(name = "Initialize Chrome")
    public void initChrome() {
        ChromeDriver driver = new ChromeDriver();
        CommandExecutor executor = driver.getCommandExecutor();

    }


}
