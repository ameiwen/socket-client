package com.client.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class StartUp {
	private static final Logger logger = LogManager.getLogger(StartUp.class);

    public static void main(String[] args) {
//        Properties prop = System.getProperties();
//        prop.setProperty("socksProxyHost", "127.0.0.1");
//        prop.setProperty("socksProxyPort", "1080");
        logger.info("Starting all tasks.");
        ProcessTask.startAllTasks();
    }

}
