package com.example.sbimgutil.schedule;

public interface ITask extends Runnable {

    /**
     * 执行前
     */
    default void before() {
    }

    /**
     * 执行后
     */
    default void after() {
    }

    default void onError(Throwable e) {
    }

    /**
     * 工作逻辑
     */
    void doWork() throws InterruptedException;

    /**
     * 入口
     */
    default void run() {
        try {
            before();
            doWork();
            after();
        } catch (Throwable e) {
            onError(e);
        }
    }
}

