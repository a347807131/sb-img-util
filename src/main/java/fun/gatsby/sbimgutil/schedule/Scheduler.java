package fun.gatsby.sbimgutil.schedule;


import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Gatsby
 * @Date: 2022-8-13 23:41
 * @Description: 多线程任务调度器
 */

@Slf4j
public class Scheduler {

    static int NEW = 0;

    static int RUNNING = 1;

    static int TERMINATED = 3;

    static int TERMINATING = 4;
    /**
     * 还在队列外等待执行的任务
     */


    private final LinkedList<Runnable> tasks = new LinkedList<>();

    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService executor;
    private volatile int state = NEW;

    public Scheduler(int nThrends, Collection<Runnable> tasks) {
        this.executor = Executors.newFixedThreadPool(nThrends);
        this.tasks.addAll(tasks);
    }

    public static Scheduler schedule(int nThrends, Collection<Runnable> tasks) {
        return new Scheduler(nThrends, tasks);
    }

    public static void scheduleNow(int nThrends, Collection<Runnable> tasks) {
        Scheduler scheduler = schedule(nThrends, tasks);
        scheduler.start();
    }

    /**
     * 立即取消所有任务,同步方法
     *
     * @return 取消执行的任务
     */
    public List<Runnable> shutDownNow() {
        return this.executor.shutdownNow();
    }

    /**
     * 等待所有任务执行完毕,在队列中的任务全部执行完毕后，才会停止线程池
     * 该方法会会一直轮询检查是否可以退出，相当于自旋锁
     */
    public void await() {
        if (state != RUNNING)
            throw new IllegalStateException("Scheduler is not running");
        //停止线程池,在队列中的任务全部执行完毕后，才会停止线程池，该方法不会阻塞
        if (!executor.isShutdown()) {
            executor.shutdown();
            state = TERMINATING;
        }
        for (; ; ) {
            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
            boolean terminated = executor.isTerminated();
            if (terminated)
                break;
            else
                Thread.yield();
        }
        state = TERMINATED;
    }

    /**
     *
     */
    public synchronized void start() {
        if (state != NEW)
            throw new IllegalStateException("Scheduler is not in state NEW");
        for (Runnable task : tasks) {
            //加入执行队列，一般情况不会阻塞
            executor.execute(task);
        }
        tasks.clear();
        state = RUNNING;
    }

}
