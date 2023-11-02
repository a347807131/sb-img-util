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
        this.outFile = outFile;
        this.cataFile = cataFile;
        this.name = "双层pdf制作 -> " + outFile.getAbsolutePath();
        this.rootPath = labelFile.getParentFile().getParentFile().toPath();
    }
    @Override
    public void doWork() throws Throwable {

        var pageTuples = new LinkedList<Tuple3<Integer, Integer, List<Label.Mark>>>();
        List<String> labelLines = Files.readAllLines(labelFile.toPath());
        for (String labelLine : labelLines) {
            Label label = Label.parse(rootPath, labelLine);
            List<Label.Mark> marks = label.getMarks();
            ImageInfo imageInfo = Imaging.getImageInfo(label.getMarkedImageFile());
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            pageTuples.add(Tuples.of(width, height, marks));
        }
        PDFUtils.createOutPdfbyPageTuples(outFile,cataFile,pageTuples);
    }
}
