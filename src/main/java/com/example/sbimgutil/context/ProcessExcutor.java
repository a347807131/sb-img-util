package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

@Slf4j
public class ProcessExcutor {

    final AppConfig appConfig;

    public static CheckPoint checkPoint;

    public static ConsoleProgressBar consoleProgressBar;


    public ProcessExcutor(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void excute() throws IOException, ExecutionException, InterruptedException {
        File tifDir = new File(appConfig.getTifDirPath());
        checkPoint = new CheckPoint(new File(appConfig.getBaseOutDirPath()));

        File[] bookDirs = tifDir.listFiles(File::isDirectory);

        if(bookDirs==null)
            throw new RuntimeException("目标tif文件夹无数据");

        var processList= appConfig.getProcessItems();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        List<AppConfig.ProcessItem> processItems = appConfig.getEnabledProcessItems();
        log.info("启动项配置中输出项的配置:{}",processItems);

        List<Runnable> tasks = new LinkedList<>();
        List<File> volumeDirsToProcess = new ArrayList<>();
        for (File bookDir : bookDirs) {
            File[] volumeDirArr = bookDir.listFiles(File::isDirectory);
            if(volumeDirArr==null)
                continue;
            var volumeDirs = new ArrayList<>(List.of(volumeDirArr));
            volumeDirs.sort(Comparator.comparing(File::getName));
            for (File volumeDir : volumeDirs) {
                List<AppConfig.ProcessItem> items = processItems.stream().filter(
                        processItem ->
                                !checkPoint.checkIfFinished(volumeDir, processItem.hashCode())
                        )
                    .collect(Collectors.toList());
                if(!items.isEmpty()){
                    String relativePath = volumeDir.getAbsolutePath().substring(tifDir.getAbsolutePath().length());
                    tasks.add(new VolumeDirProcessTask(volumeDir,relativePath,items));
                    volumeDirsToProcess.add(volumeDir);
                }
            }
        }

        int tifFileCount = FileFetchUtils.countFileRecursively(
                volumeDirsToProcess,
                VolumeDirProcessTask.supported_file_filter
        );


        log.info("共计{}卷图书，{}张tif图片待处理.",volumeDirsToProcess.size(),tifFileCount);
        consoleProgressBar = new ConsoleProgressBar(tifFileCount);
        consoleProgressBar.showCurrent();

        int workerNum = appConfig.getMaxWorkerNum();
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
        log.info("本次处理处理结束，点击enter键推出......");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
