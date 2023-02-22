package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Slf4j
@Component
public class ProcessExcutor {

    @Autowired
    AppConfig appConfig;

    public static CheckPoint checkPoint;

    public static ConsoleProgressBar consoleProgressBar;

    public void excute() throws IOException, ExecutionException, InterruptedException {
        File tifDir = new File(appConfig.getTifDirPath());
        checkPoint = new CheckPoint(new File(appConfig.getBaseOutDirPath()));

        File[] bookDirs = tifDir.listFiles(File::isDirectory);

        if(bookDirs==null)
            throw new RuntimeException("目标tif文件夹无数据");

        int tifFileCount = FileFetchUtils.countFileRecursively(
                List.of(bookDirs),
                checkPoint.getTifFileFilter());

        var processList= appConfig.getProcessList();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        List<AppConfig.ProcessConfigItem> processCfgItems = appConfig.getProcessCfgItems();

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (File bookDir : bookDirs) {
            File[] sectionDirs = bookDir.listFiles(checkPoint.getVolumeDirFilter());
            if(sectionDirs==null)
                continue;
            for (File sectionDir : sectionDirs) {
                VolumeDirProcessTask volumeDirProcessTask = new VolumeDirProcessTask(sectionDir, processCfgItems);
                tasks.add(volumeDirProcessTask);
            }
        }

        int sectionDriCount=0;
        for (File bookDir : bookDirs) {
            sectionDriCount+=FileFetchUtils.countDir(
                    bookDir,
                    checkPoint.getVolumeDirFilter()
            );
        }

        log.info("共计{}卷图书，{}张tif图片待处理.",sectionDriCount,tifFileCount);
        consoleProgressBar = new ConsoleProgressBar(tifFileCount);
        consoleProgressBar.showCurrent();

        int workerNum = appConfig.getWorkerNum();
        if(workerNum>sectionDriCount){
            workerNum=sectionDriCount;
        }
        if(workerNum>0){
            ForkJoinPool pool = new ForkJoinPool(workerNum);
            ForkJoinTask<?> forkJoinTask = pool.submit(() -> tasks.parallelStream().forEach(Runnable::run));
            //阻塞
            Object o = forkJoinTask.get();
//            Scheduler scheduler = Scheduler.scheduleNow(workerNum, tasks);
//            scheduler.await();
        }
        log.info("全部处理完成。");
    }
}
