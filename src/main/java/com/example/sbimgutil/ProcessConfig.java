package com.example.sbimgutil;

import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ProcessConfig {
    String tifDirPath;
    String baseOutDirPath;
    String pdfCataDirPath;
    String blurImagePath;
    List<Map<String,ProcessConfigItem>> processList;

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