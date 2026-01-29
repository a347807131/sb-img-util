package fun.gatsby.sbimgutil.task.builder;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.PdfMergeTask;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PdfMergeTaskBuilder extends AbstractTaskBuilder<PdfMergeTask>{
    public PdfMergeTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public PdfMergeTask build(File inFile) throws IOException {
        File[] files = inFile.listFiles();
        List<File> imgFiles = FileFilterUtils.filterList(getFileFileter(), files);
        File outFile = outFile(inFile);
        String cataDirPath = configMap.getOrDefault("cataDirPath","").toString();
        File cataFile = null;
        if (Strings.isNotBlank(cataDirPath)) {
            String cataFileName = inFile.getAbsolutePath().replace(new File(gtc.getInDir()).getAbsolutePath(), "") + ".txt";
            cataFile = new File(cataDirPath, cataFileName);
        }
        return new PdfMergeTask(imgFiles, outFile, cataFile,configMap);
    }

    @Override
    public List<File> loadInFiles() {
        LinkedHashMap<File, List<File>> dirToFilesMap = loadSortedDirToFilesMap();
        return new LinkedList<>(dirToFilesMap.keySet());
    }

    /**
     * @param dirFilesBelong 文件列表的所在目录
     * @return
     */
    public File outFile(File dirFilesBelong) {
        String outFileName = dirFilesBelong.getName() + ".pdf";
        String midpiece = dirFilesBelong.getAbsolutePath().replace(
                new File(gtc.getInDir()).getAbsolutePath(), ""
        );
        Path fleOutDirPath = Path.of(gtc.getOutDir(), midpiece);
        if (!StringUtils.isEmpty(midpiece)) {
            fleOutDirPath = fleOutDirPath.getParent();
        }
        Path outFilePath = fleOutDirPath.resolve(outFileName);
        return outFilePath.toFile();
    }
}
