package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.PDFUtils;

import java.io.File;
import java.nio.file.Path;

public class PdfImageScaleTask extends BaseTask{
    private final File pdfFile;
    private final File outFile;

    public PdfImageScaleTask(File pdfFile, File outFile){
        this.pdfFile = pdfFile;
        this.outFile = outFile;
    }

    @Override
    public void doWork() throws Throwable {
        PDFUtils.scalePdf(pdfFile, outFile, 0.25f);
    }
}
