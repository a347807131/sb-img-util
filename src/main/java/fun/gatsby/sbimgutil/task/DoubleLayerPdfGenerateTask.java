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
    private final Path rootPath;

    public DoubleLayerPdfGenerateTask(File labelFile, File cataFile, File outFile){
        this.lableFile=labelFile;
        this.name = "双层pdf制作 -> " + outFile.getAbsolutePath();
        this.outFile=outFile;
        this.cataFile = cataFile;
        this.rootPath = labelFile.getParentFile().getParentFile().toPath();
    }
    @Override
    public void doWork() throws Throwable {

        ImagesConverter imagesConverter = new ImagesConverter(lableFile.toPath().getParent().toString());
        imagesConverter.convertToBilayerPdf(imagesConverter.imageFiles(), outFile.getAbsolutePath(),cataFile);

        File xmlOutFile = new File(outFile.getParentFile(), outFile.getName() + ".xml");
        List<String> labelLines = Files.readAllLines(lableFile.toPath());
        LinkedList<Label> labels = new LinkedList<>();
        for (String labelLine : labelLines) {
            Label label = Label.parse(rootPath, labelLine);
            labels.add(label);
        }
        PDFUtils.createOutXmlbyLabels(xmlOutFile,labels);
    }
}
