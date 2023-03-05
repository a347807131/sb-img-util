package com.example.sbimgutil.schedule;


public abstract class AbstractTask implements ITask {
    /**
     * 线程池执行前
     */
    public abstract void before();

    /**
     * 线程池执行后
     */
    public abstract void after();

    public abstract void onError(Throwable e);

    /**
     * 工作逻辑
     */
    public abstract void doWork();

}
