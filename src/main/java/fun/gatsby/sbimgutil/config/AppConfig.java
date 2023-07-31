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
    int maxWorkerNum;
    String baseInDirPath;
    String baseOutDirPath;
    String blurImagePath;
    String cataDirPath;
    Map<String, ProcessTask> processTasks;

    @Data
    static public class ProcessTask {
        String taskType;
        String blurImagePath;
        int compressLimit = 500;
        String outDirPath;
        String inDirPath;
        String format;
        String fileNameRegex;
        String cataDirPath;
        String dependOn;
    }
}
