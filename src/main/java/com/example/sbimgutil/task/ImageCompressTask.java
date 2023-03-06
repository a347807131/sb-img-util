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
public class ImageCompressTask extends BaseTask{
    private final File inFilePath;
    private final File outFile;
    private int limit = 500;

    public ImageCompressTask(File inFilePath, File outFileP, int limit) {
        this.inFilePath = inFilePath;
        this.outFile = outFileP;
        this.limit = limit;
    }


    //fixme 为何压缩只要8秒，完整却需要20s
    @Override
    public void doWork() throws IOException {
        FileUtils.forceMkdirParent(outFile);
        BufferedImage bufferedImage = ImageIO.read(inFilePath);
        // FIXME: 3/6/2023
        if(bufferedImage==null){
            log.error("读取图片失败:{}",inFilePath);
            return;
        }
        float fsize = bufferedImage.getData().getDataBuffer().getSize()/(1024*1024f);
        float oriFileSizeM=fsize;
        float encoding = (float) (5.842e-6 * Math.pow(fsize, 2) - 0.002235 * fsize + 0.2732);
        float limitM = limit / 1024f;
        if (limitM == 0) {
            OutputStream os = Files.newOutputStream(outFile.toPath());
            ImageIO.write(bufferedImage, "JPG2000", os);
            os.close();
            return;
        }
        int compressTime = 0;
        while (fsize > limitM || fsize < limitM * 0.8) {
            compressTime += 1;
            OutputStream os = Files.newOutputStream(outFile.toPath());
            long s = System.currentTimeMillis();
            ImageUtils.transformImgToJp2(bufferedImage, os, 0.5f, encoding);
            os.close();
            fsize = outFile.length() / (1024 * 1024f);
            log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},耗时{}s,文件名{}",
                    compressTime,
                    fsize,
                    oriFileSizeM ,
                    encoding,
                    (System.currentTimeMillis() - s)/1000f,
                    outFile.getAbsolutePath()
            );
            if(compressTime<2)
                encoding= (limitM*0.95f)/fsize*encoding;
            else if(compressTime<5){
                System.gc();
                if (fsize > limitM)
                    encoding *=0.9;
                else if (fsize < limitM * 0.8)
                    encoding *= 1.1;
                else break;
            }else
                throw new IOException("压缩次数过多，为防爆内存，异常推出");
        }
    }
}
