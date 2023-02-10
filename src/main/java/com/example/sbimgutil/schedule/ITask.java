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
    void doWork();

    /**
     * 入口
     */
    default void run() {
        try {
            before();
            doWork();
        } catch (Throwable e) {
            onError(e);
            throw new RuntimeException(e);
        } finally {
            after();
        }
    }
}

