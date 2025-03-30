package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.PDFUtils;

import java.io.File;
import java.nio.file.Path;

public class PdfImageScaleTask extends BaseTask{
    private final File inFile;
    private final File outFile;

    public PdfImageScaleTask(File inFile, File outFile){
        super(inFile,outFile,null);
        this.inFile = inFile;
        this.outFile = outFile;
    }

    @Override
    public void doWork() throws Throwable {
        PDFUtils.scalePdf(inFile, outFile, 0.25f);
    }
}
