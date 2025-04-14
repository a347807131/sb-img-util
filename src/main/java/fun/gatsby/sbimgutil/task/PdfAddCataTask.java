package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.PDFUtils;

import java.io.File;

public class PdfAddCataTask extends BaseTask{
    private final File pdfFile;
    private final File cataFile;
    private final File outFile;

    public PdfAddCataTask(File pdfFile, File cataFile, File outFile){
        super(pdfFile,outFile,null);
        this.pdfFile = pdfFile;
        this.cataFile = cataFile;
        this.outFile = outFile;
        this.name=pdfFile.getName()+ TaskEnum.PDF_ADD_CATA.cnName;
    }
    @Override
    public void doWork() throws Throwable {
        PDFUtils.addCata(pdfFile, cataFile, outFile);
    }
}
