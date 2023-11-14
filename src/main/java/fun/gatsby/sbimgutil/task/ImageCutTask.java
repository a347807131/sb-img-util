package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.ImageCutterUtil;
import fun.gatsby.sbimgutil.utils.Label;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        this.name = TaskTypeEnum.IMAGE_CUT.taskCnName + inFile.getAbsolutePath();
    }

    public ImageCutTask(Label label, Path outDirPath) {
        this.inFile = label.getMarkedImageFile();
        this.outDirPath = outDirPath;
        rectangles = new ArrayList<>();
        this.name = TaskTypeEnum.IMAGE_CUT.taskCnName + inFile ;
        for (Label.Detection detection : label.getDetections()) {
            int[][] points = detection.getPoints();

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
            if (outFile.exists()) continue;
            if (!outFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outFile);
            }
            ImageCutterUtil.cutImage(inFile, outFile, rectangles.get(i));
        }
    }
    public static class TaskGenerator extends BaseTask.TaskGenerator {

        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask, TaskTypeEnum taskType) {
            super(gtc, processTask, taskType);
        }

        @Override
        public List<ITask> generate() throws IOException {
            String inDirPath = gtc.getInDirPath();
            File labelFile;
            if(processTask.getLabelFilePath()==null)
                labelFile = new File(inDirPath, "Label.txt");
            else
                labelFile = new File(processTask.getLabelFilePath());
            List<String> labelLines = Files.readAllLines(labelFile.toPath());
            Path parentDirPath = new File(inDirPath).getParentFile().toPath();

            List<ITask> tasks = new ArrayList<>();
            for (String labelLine : labelLines) {
                Label label = Label.parse(parentDirPath, labelLine);
                File outDir = genOutFile(label.getMarkedImageFile()).getParentFile();
                ImageCutTask imageCutTask = new ImageCutTask(label, outDir.toPath());
            }
            return tasks;
        }
    }

}

