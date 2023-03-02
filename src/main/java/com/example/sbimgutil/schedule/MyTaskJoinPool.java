package com.example.sbimgutil.schedule;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;

public class MyTaskJoinPool extends ForkJoinPool {

    public MyTaskJoinPool() {
        super();
    }


    public MyTaskJoinPool(int parallelism) {
        super(parallelism);
    }

    LinkedBlockingQueue<Node> nodes = new LinkedBlockingQueue<>();

    public void scheduleBatch(List<Runnable>... batchs) {
        for (int i = 0; i < batchs.length; i++) {
            var batch = batchs[i];
            Node node = new Node();
            node.denpendOnLast = i != 0;
            node.childrens.addAll(batch);
            nodes.add(node);
        }
    }

    public void scheduleBatch(Runnable... runnables) {
        Node node = new Node();
        node.childrens.addAll(List.of(runnables));
        nodes.add(node);
    }

    public void start() throws ExecutionException, InterruptedException {
        for (Node node : nodes) {
            node.state = TaskStateEnum.RUNNING;
            //不会阻塞
            ForkJoinTask<?> forkJoinTask = runTaskGroup(node.childrens);
            if (node.denpendOnLast)
                forkJoinTask.get();
            node.state = TaskStateEnum.FINISHED;
        }
    }

    ForkJoinTask<?> runTaskGroup(List<Runnable> tasks) throws ExecutionException, InterruptedException {
        return this.submit(() -> tasks.parallelStream().forEach(Runnable::run));
    }

    static class Node {
        boolean denpendOnLast = false;
        TaskStateEnum state = TaskStateEnum.WAITING;
        List<Runnable> childrens = new LinkedList<>();
    }

}
