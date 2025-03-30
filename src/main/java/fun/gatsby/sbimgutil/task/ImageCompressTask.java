package fun.gatsby.sbimgutil.task;

import cn.hutool.core.bean.BeanUtil;
import fun.gatsby.sbimgutil.utils.ImageUtils;
import fun.gatsby.sbimgutil.utils.PicCompressUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * jp2压缩
 */
@Slf4j
public class ImageCompressTask extends BaseTask {
    private final Integer limit;

    public ImageCompressTask(File inFile, File outFile, Map<String,Object> configMap){
        super(inFile, outFile, configMap);
        limit=configMap.get("limit")==null?0:Integer.parseInt(configMap.get("limit").toString());
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
        PicCompressUtils.imageCompress(bufferedImage, outFile, limit);
    }
}
