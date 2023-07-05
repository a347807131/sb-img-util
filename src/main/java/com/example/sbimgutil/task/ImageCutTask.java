package com.example.sbimgutil.task;

import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.ImageCutterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ImageCutTask implements ITask {

    private final File inFile;
    private final List<Rectangle> rectangles;
    private final Path outDirPath;

    public ImageCutTask(File inFile, Path outDirPath, Rectangle[] rectangles) {
        this.inFile = inFile;
        this.rectangles = Arrays.asList(rectangles);
        this.outDirPath = outDirPath;
    }

    public ImageCutTask(Label label, Path outDirPath) {
        this.inFile = label.markedImageFile;
        this.outDirPath = outDirPath;
        rectangles = new ArrayList<>();
        for (Label.Mark mark : label.getMarks()) {
            int[][] points = mark.points;

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

    @Override
    public void before() throws IOException {
    }

    @Override
    public void after() {
        log.debug("finished cutting image {} to {}", inFile, outDirPath);
    }

    @Override
    public void onError(Throwable e) {
        log.error("error", e);
    }
}

