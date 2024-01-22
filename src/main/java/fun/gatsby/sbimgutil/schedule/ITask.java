package fun.gatsby.sbimgutil.schedule;

import java.io.IOException;

public interface ITask extends Runnable {

    /**
     * 执行前
     */
    default void before() throws IOException {
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
    void doWork() throws Throwable;

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
            throw new RuntimeException(e);
        }
    }
}

