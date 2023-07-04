package com.example.sbimgutil.task;

import com.example.sbimgutil.utils.ImageCutterUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ImageCutTask extends BaseTask {

    private final File inFile;
    private final Rectangle rectangle;

    public ImageCutTask(File inFile, Path outDirPath, Rectangle rectangle) {
        this.inFile = inFile;
        this.outFile = new File(outDirPath.toFile(), inFile.getName());
        this.rectangle = rectangle;
    }

    @Override
    public void doWork() throws IOException {
        ImageCutterUtil.cutImage(inFile, outFile, rectangle);
    }
}

