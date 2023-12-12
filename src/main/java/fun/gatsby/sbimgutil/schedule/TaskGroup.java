package fun.gatsby.sbimgutil.schedule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup<T> extends LinkedList<Runnable> {

    protected final ReentrantLock preTaskLock = new ReentrantLock();
    AtomicBoolean preTaskDone = new AtomicBoolean(false);
    AtomicInteger doneTaskCount = new AtomicInteger();

    @Getter
    protected String name;

    @Getter
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

    @Setter
    protected Runnable taskPerDone = () -> {
        log.debug("name:{} 执行完成", name);
    };

    public TaskGroup() {
    }


    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        state = TaskStateEnum.CANCELLED;
    }


    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }


    /**
     *
     */
    public boolean add(Runnable task) {
        return super.add(this.wrapTask(task));
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> tasks) {
        for (Runnable task : tasks) {
            this.add(task);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Runnable> tasks) {
        for (Runnable task : tasks) {
            this.add(index, task);
        }
        return true;
    }

    @Override
    public void add(int index, Runnable element) {
        super.add(index, this.wrapTask(element));
    }

    @Override
    public void addFirst(Runnable runnable) {
        super.addFirst(this.wrapTask(runnable));
    }

    @Override
    public void addLast(Runnable runnable) {
        super.addLast(this.wrapTask(runnable));
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
                if(taskPerDone!=null) taskPerDone.run();
                int doneCount = doneTaskCount.incrementAndGet();
                if (doneCount == size() && TaskStateEnum.CANCELLED != state) {
                    state = TaskStateEnum.FINISHED;
                    if (postTask != null) postTask.run();
                }
            }
        }
    }
}