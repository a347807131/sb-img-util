package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.TranscribeTask;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TranscribeBuilder extends AbstractTaskBuilder<TranscribeTask>{
    public TranscribeBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public List<TranscribeTask> build() throws IOException {
        var timeout=(Integer)configMap.get("timeout");
        RestTemplate restTemplate = new RestTemplate();
        configMap.put("restTemplate",restTemplate);
        return super.build();
    }

    @Override
    public File outFile(File inFile) {
        return super.outFile(inFile,"srt");
    }

    Set<String> supportedExts=Set.of("mp3","wav");

    @Override
    public Set<String> getSupportedExts() {
        return supportedExts;
    }
}
