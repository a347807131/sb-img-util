package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.ImagesConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class DoubleLayerPdfGenerateTask extends BaseTask{

    private final File lableFile;

    public DoubleLayerPdfGenerateTask(File labelFile, File cataFile, File outFile){
        this.lableFile=labelFile;
        this.name = "双层pdf制作 -> " + outFile.getAbsolutePath();
        this.outFile=outFile;
    }
    @Override
    public void doWork() throws Throwable {
        ImagesConverter imagesConverter = new ImagesConverter(lableFile.toPath().getParent().toString());
        imagesConverter.convertToBilayerPdf(imagesConverter.imageFiles(), outFile.getAbsolutePath());
    }
}
