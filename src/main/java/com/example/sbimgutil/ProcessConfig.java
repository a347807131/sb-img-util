package com.example.sbimgutil;

import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ProcessConfig {
    public int imageCompressLimit=500;
    String baseDirPath;
    String baseOutDirPath;
    String blurImagePath="C:/Users/Gatsby/IdeaProjects/sb-img-util/src/main/resources/blur.png";
    public String getBlurImagePath() {
        return  this.blurImagePath;
    }
}
