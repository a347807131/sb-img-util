package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.PicCompressUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * jp2压缩
 */
@Slf4j
public class Jp2CompressTask extends BaseTask<Jp2CompressTask.Config> {

    public record Config(
        Integer limit
    ) implements Serializable {}

    public Jp2CompressTask(File inFile, File outFile, Config config){
        super(inFile, outFile, config);
    }

    @Override
    public String getName() {
        return "压缩图片: " + inFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inFile);
        if(bufferedImage==null){
            log.error("读取图片失败:{}", inFile);
            return;
        }
        PicCompressUtils.imageCompress(bufferedImage, outFile, config.limit());
    }
}
