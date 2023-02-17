package com.example.sbimgutil;

import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.Const;
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
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Component
public class ProcessExcutor {

    @Autowired
    ProcessConfig processConfig;

    private File checkPointFile;

    public void excute() {
        File tifDir = new File(processConfig.getTifDirPath());
        TifFileFilter tifFileFilter;
        try {
            this.checkPointFile=new File(processConfig.getBaseOutDirPath(),"temp.txt");
            if(!checkPointFile.exists()) {
                FileUtils.forceMkdirParent(checkPointFile);
                checkPointFile.createNewFile();
            }
            tifFileFilter = new TifFileFilter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File[] bookDirs = tifDir.listFiles(tifFileFilter);

        if(bookDirs==null)
            throw new RuntimeException("目标tif文件夹无数据");

        int tifFileCount = FileFetchUtils.countFileRecursively(List.of(bookDirs),new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith("tiff") || file.getName().endsWith("tif");
            }
        });

        var processList= processConfig.getProcessList();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        List<ProcessConfigItem> processCfgItems = processConfig.getProcessCfgItems();

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (File bookDir : bookDirs) {
            BookImageDirProcessTask bookImageDirProcessTask = new BookImageDirProcessTask(bookDir, processCfgItems);
            tasks.add(bookImageDirProcessTask);
        }

        log.info("共计{}本图书，{}张tif图片待处理.",bookDirs.length,tifFileCount);
        BookImageDirProcessTask.cpb = new ConsoleProgressBar(tifFileCount);
        BookImageDirProcessTask.cpb.showCurrent();
        BookImageDirProcessTask.checkPointFile=checkPointFile;

        int workerNum = processConfig.getWorkerNum();
        Scheduler scheduler = new Scheduler(workerNum, tasks);
        scheduler.start();
        scheduler.await();

        log.info("全部处理完成。");
    }

    class TifFileFilter implements FileFilter{

        private final HashSet<String> finishedBookDirNames;

        TifFileFilter() throws IOException {
            Collection<String> finishedBookDirNames = FileUtils.readLines(checkPointFile, Charset.defaultCharset());
            this.finishedBookDirNames = new HashSet<>(finishedBookDirNames);
        }

        @Override
        public boolean accept(File file) {
            return file.isDirectory() ;
                    //&& !finishedBookDirNames.contains(file.getName());
        }
    }
}
