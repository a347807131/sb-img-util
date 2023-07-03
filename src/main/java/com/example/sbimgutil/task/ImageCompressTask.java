package com.example.sbimgutil.task;

import lombok.extern.slf4j.Slf4j;
import com.example.sbimgutil.utils.ImageUtils;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

@Slf4j
public class ImageCompressTask extends BaseTask {
    private final File inFile;
    private int limit = 500;

    public ImageCompressTask(File inFile, File outFile, int limit) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.limit = limit;
        taskName = "压缩图片: " + inFile.getName() + " to " + outFile.getAbsolutePath();
    }


    @Override
    public void doWork() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inFile);
        if(bufferedImage==null){
            log.error("读取图片失败:{}", inFile);
            return;
        }
        ImageUtils.imageCompress(bufferedImage, outFile, limit);
    }
}
