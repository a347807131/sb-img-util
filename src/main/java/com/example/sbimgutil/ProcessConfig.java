package com.example.sbimgutil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ProcessConfig {
    int workerNum=4;
    String tifDirPath;
    String baseOutDirPath;
    String pdfCataDirPath;
    String blurImagePath;
    List<Map<String,ProcessConfigItem>> processList;

    public List<ProcessConfigItem> getProcessCfgItems(){
        List<ProcessConfigItem> processConfigItems = new ArrayList<>();
        for (Map<String, ProcessConfigItem> configItemMap : processList) {
            Set<Map.Entry<String, ProcessConfigItem>> entrySet = configItemMap.entrySet();
            Map.Entry<String, ProcessConfigItem> entry = entrySet.iterator().next();
            if(BookImageDirProcessTask.SUPORTTED_FORMATS.contains(entry.getKey())){
                entry.getValue().setFormat(entry.getKey());
                if(entry.getValue().isEnable()) {
                    processConfigItems.add(entry.getValue());
                    log.info("待处理流程项配置:{}",entry.getValue());
                }
            }else {
                log.warn("所配置的处理项格式{}不支持，目前支持格式如下:{}.",entry.getKey(),BookImageDirProcessTask.SUPORTTED_FORMATS);
            }
        }
        return processConfigItems;
    }

}
@Data
class ProcessConfigItem{
    boolean enable;
    boolean withBlur;
    String blurImagePath;
    int compressLimit;
    String outDirPath;
    String format;
    String resourceDirPath;
    String fileNameReg;
}