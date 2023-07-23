package fun.gatsby.sbimgutil.schedule;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup<T> extends AbstractTaskGroup<Runnable> {

    final ReentrantLock firstEntryLock = new ReentrantLock();

    protected volatile boolean cancelled = false;

    protected String name;

    volatile TaskStateEnum state = TaskStateEnum.NEW;

    boolean denpendOnLast = false;


    public TaskGroup() {
        int code = UUID.randomUUID().hashCode();
        this.name = "task-group-" + code;
    }

    public TaskGroup(int id, String name, Collection<? extends Runnable> tasks) {
        super(tasks);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }

    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    public void afterAllDone() {
        state = TaskStateEnum.FINISHED;
    }

    /**
     * //FIXED 可能的问题，因为不能保证方法结束前没有其他任务开始执行
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    public synchronized void beforeFirstStart() {
    }

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    public void onTaskException(Runnable task, Exception e) {
    }

    //静态代理
    class TaskProxy implements Runnable {

        final Runnable task;

        protected TaskProxy(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }
            // FIXME: 2023/7/23 在前置任务执行完毕后方可执行后续任务
            int count = taskCountAwait.decrementAndGet();
            try {
                if (count + 1 == size()) {
                    beforeFirstStart();
                }
                task.run();
            } catch (Exception e) {
                onTaskException(task, e);
            } finally {
                if (count == 0 && !cancelled) {
                    // FIXME: 2023/7/23 在其它任务全部执行完毕后方可执行后置任务
                    afterAllDone();
                }
            }
        }
    }
}