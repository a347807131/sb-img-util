package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.ChatApiTask;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class DsTaskBuilder extends AbstractTaskBuilder<ChatApiTask>{
    public DsTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public Set<String> getSupportedExts() {
        return Set.of("srt","txt");
    }

    @Override
    public File outFile(File inFile) {
        return super.outFile(inFile,"txt");
    }
}
