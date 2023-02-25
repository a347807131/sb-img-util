package com.example.sbimgutil.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.config.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    int workerNum=4;
    String tifDirPath;
    String baseOutDirPath;
    String blurImagePath;
    List<ProcessItem> processItems=null;
    Map<String,ProcessTask> processTasks;

    public List<ProcessItem> getEnabledProcessItems(){
        return processItems.stream().filter(ProcessItem::isEnable).collect(Collectors.toList());
    }

    public List<ProcessTask> getEnabledProcessTaskConfigs(){
        List<ProcessTask> enbaledTasks = processTasks.values().stream().filter(ProcessTask::isEnable).collect(Collectors.toList());
        for (ProcessTask task : enbaledTasks) {
            if (task.getDependOn() != null) {
                ProcessTask taskDepentOn = processTasks.get(task.getDependOn());
                task.setTaskDepentOn(taskDepentOn);
            }
        }

        return null;
    }

    @Data
    static public class ProcessItem{
        boolean enable;
        boolean withBlur;
        String blurImagePath;
        int compressLimit;
        String outDirPath;
        String format;
        String resourceDirPath;
        String fileNameRegex;
        String cataDirPath;
    }

    @Data
    static public class ProcessTask{
        String taskType;
        boolean enable;
        String blurImagePath;
        int compressLimit;
        String outDirPath;
        String inDirPath;
        String format;
        String fileNameRegex;
        String cataDirPath;
        String dependOn;
        ProcessTask taskDepentOn;
    }
}
