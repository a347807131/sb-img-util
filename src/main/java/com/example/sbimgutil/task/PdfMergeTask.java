package com.example.sbimgutil.task;

import com.example.sbimgutil.utils.PDFUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

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
            if (cataFile == null || !cataFile.exists()) {
                log.error("cataFile is null" + outFile.getAbsolutePath());
                throw new RuntimeException("cataFile is null" + outFile.getAbsolutePath());
            } else {
                PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
                File txtCopiedFile = new File(outFile.getParentFile(), cataFile.getName());
                Files.copy(cataFile.toPath(), txtCopiedFile.toPath());
            }
        } catch (Exception e) {
            log.error("merge pdf:{} error,目录文件:{}", outFile, cataFile, e);
            throw new RuntimeException(e);
        }
    }
}
