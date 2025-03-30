package fun.gatsby.sbimgutil.task;

import cn.hutool.core.bean.BeanUtil;
import fun.gatsby.sbimgutil.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class DrawBlurTask extends BaseTask{

    private final File blurImageFile;

    public DrawBlurTask(File inFile, File outFile, Map<String,Object> configMap){
        super(inFile, outFile, configMap);
        this.blurImageFile=new File(configMap.get("blurImageFile").toString());
    }

    @Override
    public String getName() {
        return "压缩图片: " + inFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws IOException {
        String format = inFile.getName().substring(inFile.getName().lastIndexOf(".") + 1);
        BufferedImage blurBufferedImage = ImageIO.read(blurImageFile);
        BufferedImage bufferedImage = ImageIO.read(inFile);

        float scale = bufferedImage.getHeight() / (4f * blurBufferedImage.getHeight());

        ImageUtils.drawBlurPic(bufferedImage, blurBufferedImage, scale);
        switch (format) {
            case "jp2" -> {
                ImageIO.write(bufferedImage, "jpeg2000", outFile);
            }
            case "jpg", "tif", "tiff" -> {
                ImageIO.write(bufferedImage, format, outFile);
            }
        }
    }
}
