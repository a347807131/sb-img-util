package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.task.DoubleLayerPdfGenerateTask;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class PdfTest {

    @Test
    public void t1(){
        DoubleLayerPdfGenerateTask task = new DoubleLayerPdfGenerateTask(
                Path.of("D:\\五经五卷-0007\\0007","Label.txt").toFile(),
                null,
                new File("D:/out.pdf")
        );
        task.run();
    }
}
