package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.ImagesConverter;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DoubleLayerPdfGenerateTask extends BaseTask{

    private final File lableFile;
    private final File cataFile;
    private final Path datasetPath;
    private final File outXmlFile;
    private final File outPdfFile;

    public DoubleLayerPdfGenerateTask(File labelFile, File cataFile, File outPdfFile,File outXmlFile){
        this.lableFile=labelFile;
        this.name = "双层pdf制作 -> " + outPdfFile.getAbsolutePath();
        this.outPdfFile=outPdfFile;
        this.outXmlFile=outXmlFile;
        this.cataFile = cataFile;
        this.datasetPath = labelFile.getParentFile().getParentFile().toPath();
    }
    @Override
    public void doWork() throws Throwable {
        List<String> labelLines = Files.readAllLines(lableFile.toPath());
        LinkedList<Label> labels = new LinkedList<>();
        for (String labelLine : labelLines) {
            Label label = Label.parse(datasetPath, labelLine);
            labels.add(label);
        }

        PDFUtils.createOutXmlbyLabels(outXmlFile,labels);
        ImagesConverter imagesConverter = new ImagesConverter(labels,cataFile);
        imagesConverter.convertToBilayerPdf(outPdfFile);
    }
}
