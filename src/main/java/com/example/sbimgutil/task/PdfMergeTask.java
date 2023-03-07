package com.example.sbimgutil.task;

import com.example.sbimgutil.utils.PDFUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

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
            } else {
                PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
                FileUtils.copyFile(cataFile, new File(outFile.getParentFile(), cataFile.getName()));
            }
        } catch (Exception e) {
            log.error("merge pdf:{} error",outFile, e);
            throw new RuntimeException(e);
        }
    }
}
