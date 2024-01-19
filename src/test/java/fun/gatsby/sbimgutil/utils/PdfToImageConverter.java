package fun.gatsby.sbimgutil.utils;

import java.io.File;
import java.io.IOException;

public class PdfToImageConverter {
    public static void main(String[] args) throws IOException {
        String pdfFilePath = "D:\\temp\\0001.pdf";
        String outputDirPath = "build/";
        PDFUtils.split(new File(pdfFilePath), new File(outputDirPath).toPath());

    }
}