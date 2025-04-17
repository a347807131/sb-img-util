package fun.gatsby.sbimgutil.task.builder;


import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.Mp3ExtractTask;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class SoundtrackExtractTaskBuilder extends AbstractTaskBuilder<Mp3ExtractTask>   {

    public SoundtrackExtractTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public Set<String> getSupportedExts() {
        return Set.of("mp4","avi","mkv","mov","wmv","flv","rmvb","rm","3gp","mpg","mpeg");
    }

    @Override
    public File outFile(File inFile) {
        return super.outFile(inFile,"mp3");
    }
}
