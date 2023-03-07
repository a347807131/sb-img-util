package com.example.sbimgutil.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class TifUtilsTest {
    String jpdDirPath="C:\\Users\\Gatsby\\datasets\\图片处理模板\\1 扫描原图（TIFF格式）\\27030166\\0001";

    @Test
    void tranformImgToTiff() throws IOException {

        File file = new File(jpdDirPath);
        File[] files = file.listFiles();
        for (File imgFile : files) {
            log.info("原文件大小,{}",imgFile.length()/(1024*1024f));
            BufferedImage read = ImageIO.read(imgFile);
            File outF = new File("test");
            ImageUtils.transformImgToJpg(read, new FileOutputStream(outF),0);
            log.info("转换jpg后文件大小,{}",outF.length()/(1024*1024f));
            read = ImageIO.read(new File("test"));
            ImageIO.write(read,"JPEG2000",new File("test"));
            log.info("转换tiff后文件大小,{}",outF.length()/(1024*1024f));
        }
    }
}
