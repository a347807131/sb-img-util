package com.example.sbimgutil;

import com.example.sbimgutil.config.ProcessConfig;
import com.example.sbimgutil.context.CheckPoint;
import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
@Component
public class ProcessExcutor {

    @Autowired
    ProcessConfig processConfig;

    public static File checkPointFile;

    public static CheckPoint checkPoint;

    public void excute() throws IOException {
        File tifDir = new File(processConfig.getTifDirPath());
        checkPoint = new CheckPoint(new File(processConfig.getBaseOutDirPath()));

        File[] bookDirs = tifDir.listFiles(File::isDirectory);

        if(bookDirs==null)
            throw new RuntimeException("目标tif文件夹无数据");

        int tifFileCount = FileFetchUtils.countFileRecursively(
                List.of(bookDirs),
                checkPoint.getTifFileFilter());

        var processList= processConfig.getProcessList();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        List<ProcessConfig.ProcessConfigItem> processCfgItems = processConfig.getProcessCfgItems();

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (File bookDir : bookDirs) {
            BookImageDirProcessTask bookImageDirProcessTask = new BookImageDirProcessTask(bookDir, processCfgItems);
            tasks.add(bookImageDirProcessTask);
        }

//        // FIXME: 2/21/2023 该方法一直返回0，有问题
//        int sectionDriCount = FileFetchUtils.countFileRecursively(
//                List.of(bookDirs),
//                checkPoint.getSectionDirFilter()
//        );

        int sectionDriCount=0;
        for (File bookDir : bookDirs) {
            sectionDriCount+=FileFetchUtils.countDir(
                    bookDir,
                    checkPoint.getSectionDirFilter()
            );
        }

        log.info("共计{}卷图书，{}张tif图片待处理.",sectionDriCount,tifFileCount);
        BookImageDirProcessTask.cpb = new ConsoleProgressBar(tifFileCount);
        BookImageDirProcessTask.cpb.showCurrent();

        int workerNum = processConfig.getWorkerNum();
        Scheduler scheduler = new Scheduler(workerNum, tasks);
        scheduler.start();
        scheduler.await();

        log.info("全部处理完成。");
    }
}
