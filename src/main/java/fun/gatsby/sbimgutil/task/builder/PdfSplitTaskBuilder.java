package fun.gatsby.sbimgutil.task.builder;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.PdfSplitTask;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PdfSplitTaskBuilder extends AbstractTaskBuilder<PdfSplitTask>{
    public PdfSplitTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public PdfSplitTask build(File inFile) throws IOException {
        var outDir = outFile(inFile,"pdf").toPath();
        return new PdfSplitTask(inFile, outDir);
    }
}
