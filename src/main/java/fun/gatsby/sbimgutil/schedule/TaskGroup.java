package fun.gatsby.sbimgutil.schedule;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup<T> extends AbstractTaskGroup<Runnable> {

    protected final ReentrantLock preTaskLock = new ReentrantLock();
    AtomicBoolean preTaskDone = new AtomicBoolean(false);
    AtomicInteger doneTaskCount = new AtomicInteger();

    protected String name;

    volatile TaskStateEnum state = TaskStateEnum.NEW;

    /**
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    protected Runnable preTask = () -> {
        log.debug("name:{} 开始执行", name);
    };
    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    protected Runnable postTask = () -> {
        log.debug("name:{} 执行完成", name);
    };

    public TaskGroup() {
    }

    public TaskGroup(Collection<? extends Runnable> taskQueue) {
        super(taskQueue);
    }

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        state = TaskStateEnum.CANCELLED;
    }


    @Override
    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }

    public void setPreAndPostTasks(Runnable pre, Runnable post) {
        this.preTask = pre;
        this.postTask = post;
    }


    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    protected void onTaskException(Runnable task, Exception e) {
    }

    //静态代理
    class TaskProxy implements Runnable {

        final Runnable task;

        protected TaskProxy(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (TaskStateEnum.CANCELLED == state) {
                return;
            }

            if (!preTaskDone.get()) {
                preTaskLock.lock();
                if (!preTaskDone.get()) {
                    if (preTask != null) preTask.run();
                    preTaskDone.compareAndSet(false, true);
                }
                preTaskLock.unlock();
            }
            try {
                task.run();
            } catch (Exception e) {
                onTaskException(task, e);
            } finally {
                int doneCount = doneTaskCount.incrementAndGet();
                if (doneCount == size() && TaskStateEnum.CANCELLED != state) {
                    state = TaskStateEnum.FINISHED;
                    if (postTask != null) postTask.run();
                }
            }
        }
    }
}