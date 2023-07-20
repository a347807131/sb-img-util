package fun.gatsby.sbimgutil.schedule;


import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author Gatsby
 * @see <img src="https://img-blog.csdnimg.cn/883009304fb942b2b20a14d14d85c9fd.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAaGFvaGFvX2Rpbmc=,size_20,color_FFFFFF,t_70,g_se,x_16">
 */

@Deprecated
@Slf4j
public class SchedulerDeprecated {

    /**
     * 执行任务队列数组
     */
    private final LinkedBlockingQueue<Runnable> blockingTaskQueue;
    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService loopExecutor;
    /**
     * 允许并行执行的线程数,数值为并发量加1，额外一条用于添加指向性任务
     */
    private final int nThrends;
    /**
     * 还在队列外等待执行的任务
     */
    private final LinkedList<Runnable> tasks;

    /**
     * 队列的容量
     */
    int queueSize;

    private volatile boolean cancelled = false;

    /**
     * 构造器
     *
     * @param nThrends  同时执行任务中的任务线程数
     * @param queueSize 任务执行队列
     */
    public SchedulerDeprecated(int nThrends, int queueSize, List<Runnable> tasks) {
        this.tasks = new LinkedList<>(tasks);
        this.nThrends = nThrends;
        this.queueSize = queueSize;
        this.loopExecutor = Executors.newFixedThreadPool(this.nThrends);
        this.blockingTaskQueue = new LinkedBlockingQueue<>(queueSize);
    }

    public SchedulerDeprecated(int nThrends, List<Runnable> tasks) {
        this(nThrends, 2 << 7, tasks);
    }

    public SchedulerDeprecated(int nThrends, Runnable... tasks) {
        this(nThrends, 1, new LinkedList<>(Arrays.asList(tasks)));
    }

    public int getnThrends() {
        return nThrends;
    }

    /**
     * 立即取消所有任务
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * 执行方法
     */
    public void run() {

        putTaskToQueueAsync();
        takeAndExecuteSync();

        //停止线程池,在队列中的任务全部执行完毕后，才会停止线程池，该方法不会阻塞
        if (!cancelled)
            loopExecutor.shutdown();
        else
            loopExecutor.shutdownNow();
        for (; ; ) {
            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
            if (this.loopExecutor.isTerminated()) {
                log.info("全部任务执行完毕,关闭线程池");
                break;
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * 循环执行执行任务,在没有额外任务可执行时退出循环
     */
    private void takeAndExecuteSync() {
        while (!cancelled) {
            // 所有执行任务都放入队列后，退出
            if (this.tasks.isEmpty()) {
                if (this.blockingTaskQueue.isEmpty()) {
                    return;
                }
            }
            // 获取一个执行任务,并执行
            try {
                if (blockingTaskQueue.isEmpty()) {
                    Thread.yield();
                    continue;
                }
                var task = this.blockingTaskQueue.take();
                this.loopExecutor.execute(task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 开启一个线程，持续向执行任务队列添加执行任务，直到所有的任务任务添加完
     */
    private void putTaskToQueueAsync() {
        new Thread(() -> {
            while (!cancelled) {
                // 获取添加执行任务的的任务索引值
                try {
                    Runnable task = tasks.poll();
                    if (task == null) {
                        return;
                    }
                    this.blockingTaskQueue.put(task);
                } catch (InterruptedException e) {
                    log.error("向执行任务队列放入任务异常", e);
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
