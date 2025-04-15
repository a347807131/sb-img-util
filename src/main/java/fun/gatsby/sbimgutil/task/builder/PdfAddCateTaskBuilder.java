package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.PdfAddCataTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class PdfAddCateTaskBuilder extends AbstractTaskBuilder<PdfAddCataTask>{
    public PdfAddCateTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public PdfAddCataTask build(File inFile) throws IOException {
        var outFile = outFile(inFile);
        String txtFileRelativePath = inFile.getAbsolutePath().replace(
                new File(gtc.getInDir()).getAbsolutePath(), "./"
        ).replace(".pdf", ".txt");
        String cataDirPath = configMap.get("cataDirPath").toString();
        var cataFile = Path.of(cataDirPath, txtFileRelativePath).toFile();
        return  new PdfAddCataTask(inFile, cataFile, outFile);
    }
}
