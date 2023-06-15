package com.example.sbimgutil.task;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.Const;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class ImageTransformTask extends BaseTask{

    private final File inFile;
    private final String format;


    public ImageTransformTask(File inFile,File outFile,String format) {
        this.inFile = inFile;
        this.format = format;
        this.outFile=outFile;
        taskName= "格式转换: "+inFile.getName()+" to "+outFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inFile);
        switch (format) {
            case "jp2" -> {
                ImageIO.write(bufferedImage, "jpeg2000", outFile);
            }
            case "jpg", "tif", "tiff" -> {
                ImageIO.write(bufferedImage, format, outFile);
            }
        }
        var bufferedOutImage = ImageIO.read(outFile);
        if (bufferedOutImage.getWidth() != bufferedImage.getWidth() || bufferedOutImage.getHeight() != bufferedImage.getHeight()) {
            log.error("转换失败,存在输入输出大小不一致的问题: {} to {}", inFile.getAbsolutePath(), outFile.getAbsolutePath());
        }
    }

}
