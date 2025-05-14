package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.TextToBlogMdTask;

import javax.sound.midi.Patch;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class TextToBlogMdTaskBuilder extends AbstractTaskBuilder<TextToBlogMdTask>{
    public TextToBlogMdTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public File outFile(File inFile) {
        String outName = inFile.getName().split("\\.")[0] + ".md";
        String outDir = gtc.getOutDir();
        return Path.of(outDir).resolve(outName).toFile();
    }

    @Override
    public Set<String> getSupportedExts() {
        return Set.of("txt","md");
    }
}
