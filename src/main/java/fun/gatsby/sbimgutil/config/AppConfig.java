package fun.gatsby.sbimgutil.config;

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
    String readmeUrl;
    String version;
    String author;
    String name;
    String loggingFilePath;

    Map<String, ProcessTask> processTasks;

    GlobalTaskConfig globalTaskConfig;

    @Data
    static public class GlobalTaskConfig {
        int maxWorkerNum;
        String inDirPath;
        String outDirPath;
        boolean recursive = true;
        String fileNameRegex;
        boolean enforce=false;
    }

    @Data
    static public class ProcessTask {
        String blurImagePath;
        int compressLimit = 500;
        String format;
        String cataDirPath;
        String labelFilePath;
    }
}
