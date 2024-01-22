package fun.gatsby.sbimgutil.schedule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一批一批的地执行任务
 */
public class TaskScheduleForkJoinPool extends ForkJoinPool {

    public TaskScheduleForkJoinPool() {
        super();
    }


    public TaskScheduleForkJoinPool(int parallelism) {
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
            if(node.state == TaskStateEnum.FINISHED)
                continue;
            //不会阻塞
            ForkJoinTask<?> forkJoinTask =
                    this.submit(() -> node.childrens.parallelStream().forEach(Runnable::run));
            node.state = TaskStateEnum.RUNNING;
//            if (node.denpendOnLast)
            try {
                forkJoinTask.get();
                node.state = TaskStateEnum.FINISHED;
            } catch (Exception e) {
                node.state=TaskStateEnum.ERROR;
            }
        }
    }

    static class Node {
        boolean denpendOnLast = false;
        TaskStateEnum state = TaskStateEnum.WAITING;
        List<Runnable> childrens = new LinkedList<>();
    }

    public int getTaskCount(){
        return nodes.stream().mapToInt(e->e.childrens.size()).sum();
    }
}
