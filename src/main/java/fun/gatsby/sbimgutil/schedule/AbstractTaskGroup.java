package fun.gatsby.sbimgutil.schedule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTaskGroup<R> extends LinkedList<Runnable> {

    protected volatile TaskStateEnum state = TaskStateEnum.NEW;

    public TaskStateEnum getState() {
        return state;
    }

    public AbstractTaskGroup() {

    }

    public AbstractTaskGroup(Collection<? extends Runnable> taskQueue) {
        this.addAll(taskQueue);
    }

    /**
     *
     */
    @Override
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
     * 获取task的包装类，默认不做任何操作
     *
     * @param task 源任务
     * @return 包装类实例
     */
    protected Runnable wrapTask(Runnable task) {
        return task;
    }
}