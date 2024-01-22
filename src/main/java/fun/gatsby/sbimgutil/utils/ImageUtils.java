package fun.gatsby.sbimgutil.utils;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class ImageUtils {
    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage){
        Point point = new Point((oriBufferedImage.getWidth() - blurBufferedImage.getWidth()) / 2, (oriBufferedImage.getHeight() - blurBufferedImage.getHeight()) / 2);
        Graphics g = oriBufferedImage.getGraphics();
        g.drawImage(blurBufferedImage, point.x, point.y, null);
        g.dispose();
    }

    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage, float scale) throws IOException {
        BufferedImage scaledBlurBufferedImage = Thumbnails.of(blurBufferedImage).scale(scale).asBufferedImage();
        drawBlurPic(oriBufferedImage, scaledBlurBufferedImage);
    }

    public static BufferedImage scale(BufferedImage bf,float scale){
        AffineTransform affineTransform = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(affineTransform, null);
        return op.filter(bf, null);
    }
}
