package com.example.sbimgutil;

import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.Task;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@SpringBootTest
public class FullProcessTest {
    @Autowired
    ProcessConfig processConfig;

    @Test
    public void t1(){
        String baseDirPath = processConfig.getBaseDirPath();
        File tifDir = new File(baseDirPath, Const.tifDirName);

        FileFilter tifFileFilter = File::isDirectory;
        File[] files = tifDir.listFiles(tifFileFilter);
        if(files==null)
            throw new RuntimeException("目标tif文件夹无数据");

        var processList= processConfig.getProcessList();
        if(processList.isEmpty()) {
            log.info("启动项配置中没有输出项的配置,请检查配置文件");
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

        System.out.println("开始处理批任务");
        BookImageDirProcessTask.cpb = new ConsoleProgressBar(tasks.size());
        tasks.parallelStream().forEach(Runnable::run);
    }
}