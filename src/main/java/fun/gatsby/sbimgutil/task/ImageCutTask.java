package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.ImageCutterUtil;
import fun.gatsby.sbimgutil.utils.Label;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ImageCutTask extends BaseTask {

    private final File inFile;
    private final List<Rectangle> rectangles;
    private final Path outDirPath;

    public ImageCutTask(File inFile, Path outDirPath, Rectangle[] rectangles) {
        this.inFile = inFile;
        this.rectangles = Arrays.asList(rectangles);
        this.outDirPath = outDirPath;
        this.taskName = TaskTypeEnum.IMAGE_CUT.taskCnName + inFile + "->" + outDirPath;
    }

    public ImageCutTask(fun.gatsby.sbimgutil.utils.Label label, Path outDirPath) {
        this.inFile = label.getMarkedImageFile();
        this.outDirPath = outDirPath;
        rectangles = new ArrayList<>();
        this.taskName = TaskTypeEnum.IMAGE_CUT.taskCnName + inFile + "->" + outDirPath;
        for (Label.Mark mark : label.getMarks()) {
            int[][] points = mark.getPoints();

            int width = Math.abs(points[1][0] - points[0][0]);
            int height = Math.abs(points[2][1] - points[0][1]);

            int x = (points[0][0] + points[1][0] - width) / 2;
            int y = (points[2][1] + points[0][1] - height) / 2;
            Point point = new Point(x, y);

            Rectangle rectangle = new Rectangle(point, new Dimension(Math.abs(width), Math.abs(height)));
            rectangles.add(rectangle);
        }

    }


    @Override
    public void doWork() throws IOException {
        for (int i = 0; i < rectangles.size(); i++) {
            String fileName = inFile.getName().replace(".", "-" + i + ".");
            File outFile = new File(outDirPath.toFile(), fileName);
            if (!outFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outFile);
            }
            ImageCutterUtil.cutImage(inFile, outFile, rectangles.get(i));
        }
    }
}

