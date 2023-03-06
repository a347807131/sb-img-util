package com.example.sbimgutil.task;

import com.example.sbimgutil.utils.PDFUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class PdfMergeTask extends BaseTask {

    private final List<File> inFiles;
    private File cataFile=null;

    public PdfMergeTask(List<File> inFiles, File outFile, File cataFile) {
        this.inFiles = inFiles;
        this.outFile = outFile;
        this.cataFile = cataFile;
    }
    public PdfMergeTask(List<File> inFiles, File outFile) {
        this.inFiles = inFiles;
        this.outFile = outFile;
    }

    @Override
    public void doWork() {
        try (OutputStream os = Files.newOutputStream(outFile.toPath())) {
            if (cataFile == null) {
                PDFUtils.mergeIntoPdf(inFiles, os);
            } else {
                PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
            }
        } catch (Exception e) {
            log.error("merge pdf:{} error",outFile, e);
            throw new RuntimeException(e);
        }
    }
}
