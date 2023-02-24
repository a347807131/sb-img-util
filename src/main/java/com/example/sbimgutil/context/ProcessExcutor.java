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

        var processList= appConfig.getProcessList();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        List<AppConfig.ProcessConfigItem> processCfgItems = appConfig.getEnabledProcessCfgItems();

        List<File> volumeDirsToProcess = new ArrayList<>();
        for (File bookDir : bookDirs) {
            File[] volumeDirs = bookDir.listFiles(File::isDirectory);
            if(volumeDirs==null)
                continue;
            volumeDirsLool:
            for (File volumeDir : volumeDirs) {
                for (AppConfig.ProcessConfigItem processCfgItem : processCfgItems) {
                    boolean finished=checkPoint.checkIfFinished(volumeDir,processCfgItem.hashCode());
                    if(!finished){
                        volumeDirsToProcess.add(volumeDir);
                        continue volumeDirsLool;
                    }
                }
            }
        }

        int tifFileCount = FileFetchUtils.countFileRecursively(volumeDirsToProcess, checkPoint.getTifFileFilter());

        Collection<Runnable> tasks = new LinkedList<>();
        volumeDirsToProcess.forEach(e->tasks.add(new VolumeDirProcessTask(e,processCfgItems)));

        log.info("共计{}卷图书，{}张tif图片待处理.",volumeDirsToProcess.size(),tifFileCount);
        consoleProgressBar = new ConsoleProgressBar(tifFileCount);
        consoleProgressBar.showCurrent();

        int workerNum = appConfig.getWorkerNum();
        if (workerNum> tasks.size()) {
            workerNum = tasks.size();
        }

        if(workerNum>0){
            ForkJoinPool pool = new ForkJoinPool(workerNum);
            ForkJoinTask<?> forkJoinTask = pool.submit(() -> tasks.parallelStream().forEach(Runnable::run));
            //阻塞
            Object o = forkJoinTask.get();
//            Scheduler scheduler = Scheduler.scheduleNow(workerNum, tasks);
//            scheduler.await();
        }
        log.info("处理结束。");
    }
}
