package com.example.sbimgutil.task;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.FileFetchUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.example.sbimgutil.context.VolumeDirProcessTask.supported_file_filter;

public class ImageTransformTask extends BaseTask{


    public static final Set<String> SUPORTTED_FORMATS = Set.of("pdf", "jp2", "jpg","tif","tiff");
    private final File inFile;
    private final File outFile;
    private final String format;


    public ImageTransformTask(File inFile,File outFile,String format) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.format = format;
    }

    @Override
    public void doWork() {
        switch (format) {
            case "jpeg2000","jpg", "tif", "tiff" -> {
                try {
                    BufferedImage bufferedImage = ImageIO.read(inFile);
                    ImageIO.write(bufferedImage, format, outFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
