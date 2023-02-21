package com.example.sbimgutil.schedule;

import lombok.var;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTaskGroup extends LinkedList<Runnable> {

    /**
     * 剩余未完成的任务的数量
     */
    protected AtomicInteger taskCountAwaitingToFinish = new AtomicInteger(0);

    public AbstractTaskGroup() {
    }

    public AbstractTaskGroup(Collection<? extends Runnable> taskQueue) {
        this.addAll(taskQueue);
        this.taskCountAwaitingToFinish.addAndGet(taskQueue.size());
    }

    @Override
    public boolean add(Runnable task) {
        var taskWrapper = this.wrapTask(task);
        this.taskCountAwaitingToFinish.addAndGet(1);
        return super.add(taskWrapper);
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> tasks) {
        var list = new LinkedList<Runnable>();
        for (Runnable task : tasks) {
            list.add(this.wrapTask(task));
        }
        this.taskCountAwaitingToFinish.addAndGet(list.size());
        return super.addAll(list);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Runnable> tasks) {
        var list = new LinkedList<Runnable>();
        for (Runnable task : tasks) {
            list.add(this.wrapTask(task));
        }
        return super.addAll(index, list);
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