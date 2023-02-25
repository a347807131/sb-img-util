package com.example.sbimgutil.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

//@Slf4j
//@Data
//@Configuration
//@ConfigurationProperties(prefix = "app")
public class Config {

    List<ProcessTask> processTasks;

    @Data
    static public class ProcessTask{
        String taskId;
        String taskType;
        boolean enable;
        String blurImagePath;
        int compressLimit;
        String outDirPath;
        String inDirPath;
        String format;
        String fileNameRegex;
        String cataDirPath;
        Integer dependOn;
    }
}
