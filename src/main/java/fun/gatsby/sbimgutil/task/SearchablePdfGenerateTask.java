package fun.gatsby.sbimgutil.task;

import cn.hutool.core.lang.Tuple;
import fun.gatsby.lang.tuple.Tuple3;
import fun.gatsby.lang.tuple.Tuples;
import fun.gatsby.sbimgutil.utils.Const;
import fun.gatsby.sbimgutil.utils.FileFetchUtils;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SearchablePdfGenerateTask extends BaseTask{

    private final File cataFile;
    private final File labelFile;
    private final Path rootPath;

    public SearchablePdfGenerateTask(File outFile, File labelFile , File cataFile){
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
        PDFUtils.createOutXmlbyPageTuples( outFile,cataFile,labels);
    }
}
