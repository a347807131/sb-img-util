package fun.gatsby.sbimgutil.task;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
