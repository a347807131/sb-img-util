package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.utils.PDFUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class OcrRetXmlGenerateTask extends BaseTask{

    private final File cataFile;
    private final File labelFile;
    private final Path rootPath;

    public OcrRetXmlGenerateTask(File outFile, File labelFile , File cataFile){
        this.labelFile=labelFile;
        this.outFile = new File(outFile.getAbsolutePath()+ ".xml");
        this.cataFile = cataFile;
        this.name = "双层pdf制作 -> " + outFile.getAbsolutePath();
        this.rootPath = labelFile.getParentFile().getParentFile().toPath();
    }
    @Override
    public void doWork() throws Throwable {

        List<String> labelLines = Files.readAllLines(labelFile.toPath());
        LinkedList<Label> labels = new LinkedList<>();
        for (String labelLine : labelLines) {
            Label label = Label.parse(rootPath, labelLine);
            labels.add(label);
        }
        PDFUtils.createOutXmlbyLabels( outFile,cataFile,labels);
    }
}
