package com.client.socket;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ProcessTask extends Thread {

	private static final Logger logger = LogManager.getLogger(ProcessTask.class);

    private boolean shutdown = false;

    private boolean down = false;

    private static final List<ProcessTask> processTasks = new CopyOnWriteArrayList<ProcessTask>();

    public abstract void process() throws Exception;

    public static void startAllTasks() {

        // okcoin
        OkCoinPublishTask okCoinPublishTask = new OkCoinPublishTask();
        okCoinPublishTask.setName("OkCoinPublishTask");
        okCoinPublishTask.start();
    }
    
	@Override
	public void run() {
		processTasks.add(this);
        try {
            init();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        while (!down && !shutdown) {
            try {
                process();
            } catch (InterruptedException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        processTasks.remove(this);
        try {
            end();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
	}
	
	public abstract void init() throws Exception;

    public abstract void end() throws Exception;

    public static int getTaskSize() {
        return processTasks.size();
    }

    public static void shutDownAll() {
        for (ProcessTask processTask : processTasks) {
            processTask.shutdown();
        }
    }

    public static void downAll() {
        for (ProcessTask processTask : processTasks) {
            processTask.down();
        }
    }

    public void shutdown() {
        this.setShutdown(true);
        try {
            this.interrupt();
        } catch (Exception e) {
        }
    }

    public void down() {
        this.setDown(true);
        try {
            this.interrupt();
        } catch (Exception e) {

        }
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean isDown() {
        return down;
    }

}
