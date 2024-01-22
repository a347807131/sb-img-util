package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.ImageUtils;
import fun.gatsby.sbimgutil.utils.PicCompressUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * jp2压缩
 */
@Slf4j
public class ImageCompressTask extends BaseTask {
    private final File inFile;
    private int limit = 500;

    public ImageCompressTask(File inFile, File outFile, int limit) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.limit = limit;
        name = "压缩图片: " + inFile.getAbsolutePath();
    }


    @Override
    public void doWork() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inFile);
        if(bufferedImage==null){
            log.error("读取图片失败:{}", inFile);
            return;
        }
        PicCompressUtils.imageCompress(bufferedImage, outFile, limit);
    }
}
