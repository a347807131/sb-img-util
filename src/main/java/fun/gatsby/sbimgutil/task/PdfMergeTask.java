package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.PDFUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
@Slf4j
public class PdfMergeTask extends BaseTask {

    private List<File> inFiles;
    private File cataFile=null;

    public PdfMergeTask(List<File> inFiles, File outFile, File cataFile) {
        this.inFiles = inFiles;
        this.outFile = outFile;
        this.cataFile = cataFile;
        this.taskName = "合并pdf: " + outFile.getAbsolutePath();
    }
    public PdfMergeTask(List<File> inFiles, File outFile) {
        this.inFiles = inFiles;
        this.outFile = outFile;
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
                PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
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
