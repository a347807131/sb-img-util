package com.example.sbimgutil;

import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.Const;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Slf4j
@SpringBootApplication
public class SbImgUtilApplication {




    public static void main(String[] args) {
        var ctx = new SpringApplicationBuilder(SbImgUtilApplication.class)
                .headless(false).run(args);
        SbImgUtilApplication bean = ctx.getBean(SbImgUtilApplication.class);
        bean.excute();
    }

    @Autowired
    ProcessConfig processConfig;
    void excute(){
        String baseDirPath = processConfig.getBaseDirPath();
        File tifDir = new File(baseDirPath, Const.tifDirName);
        int tifFileCount = FileFetchUtils.countFileRecursively(tifDir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory()||file.getName().endsWith(".tif");
            }
        });

        FileFilter tifFileFilter = File::isDirectory;
        File[] files = tifDir.listFiles(tifFileFilter);
        if(files==null)
            throw new RuntimeException("目标tif文件夹无数据");

        var processList= processConfig.getProcessList();
        if(processList.isEmpty()) {
            log.warn("启动项配置中没有输出项的配置,请检查配置文件");
            return;
        }

        ArrayList<ProcessConfigItem> processConfigItems = new ArrayList<>();
        for (Map<String, ProcessConfigItem> configItemMap : processList) {
            Set<Map.Entry<String, ProcessConfigItem>> entrySet = configItemMap.entrySet();
            Map.Entry<String, ProcessConfigItem> entry = entrySet.iterator().next();
            if(BookImageDirProcessTask.SUPORTTED_FORMATS.contains(entry.getKey())){
                entry.getValue().setFormat(entry.getKey());
                if(entry.getValue().isEnable())
                    processConfigItems.add(entry.getValue());
            }else {
                log.warn("所配置的处理项格式{}不支持，目前支持格式如下:{}.",entry.getKey(),BookImageDirProcessTask.SUPORTTED_FORMATS);
            }
        }

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (File bookDir : files) {
            BookImageDirProcessTask bookImageDirProcessTask = new BookImageDirProcessTask(bookDir, processConfigItems);
            tasks.add(bookImageDirProcessTask);
        }

        log.info("开始处理批任务");
        BookImageDirProcessTask.cpb = new ConsoleProgressBar(tifFileCount);
        BookImageDirProcessTask.cpb.showCurrent();

//        Scheduler scheduler = new Scheduler(8,tasks);
//        scheduler.start();
//        scheduler.await();
        tasks.parallelStream().forEach(Runnable::run);
        log.info("全部处理完成。");
    }
}
