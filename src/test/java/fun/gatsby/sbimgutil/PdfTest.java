package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.task.DoubleLayerPdfGenerateTask;
import fun.gatsby.sbimgutil.task.PdfImageScaleTask;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class PdfTest {

    @Test
    public void t1(){

        var dir=Path.of("D:\\out\\27030166");
        var outFile=dir.resolve("0001_0.25.pdf").toFile();
        PdfImageScaleTask pdfImageScaleTask = new PdfImageScaleTask(dir.resolve("0001.pdf").toFile(),outFile);
        pdfImageScaleTask.run();
    }
}
