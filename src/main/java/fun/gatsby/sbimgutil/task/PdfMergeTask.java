package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

@Slf4j
public class PdfMergeTask extends BaseTask<Object> {

    private List<File> inFiles;
    private File cataFile;

    private float imageScale=1f;

    public PdfMergeTask(List<File> inFiles, File outFile, File cataFile, Map<String,Object> configMap) {
        super(null, outFile, configMap);
        this.inFiles = inFiles;
        this.cataFile =cataFile;
        this.imageScale=configMap.get("pdfImageScale")==null?1f:(float)configMap.get("pdfImageScale");
    }

    @Override
    public String getName() {
        return "合并pdf -> " + outFile.getAbsolutePath();
    }

    @Override
    public void doWork() {
        inFiles = inFiles.stream().sorted(Comparator.comparing(File::getName)).toList();
        try (OutputStream os = Files.newOutputStream(outFile.toPath())) {
            if (cataFile == null) {
                PDFUtils.mergeIntoPdf(inFiles, os);
                return;
            } else if (!cataFile.exists()) {
                log.error("cataFile is null" + outFile.getAbsolutePath());
                throw new IOException("cataFile is null" + outFile.getAbsolutePath());
            } else {
                if (imageScale >= 1)
                    PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
                else
                    PDFUtils.mergeIntoPdfWithScale(inFiles, cataFile, os, imageScale);
                File copiedCataFile = new File(outFile.getParentFile(), cataFile.getName());
                Files.copy(cataFile.toPath(), copiedCataFile.toPath());
                return;
            }
        } catch (Exception e) {
            log.error("merge pdf:{} error", outFile, e);
            throw new RuntimeException(e);
        }
    }
}
