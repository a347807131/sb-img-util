package com.example.sbimgutil;

import com.example.sbimgutil.task.ImageCutTask;
import com.example.sbimgutil.utils.ImageCutterUtil;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImgCropTest {

    @Test
    public void t1() throws IOException {
        Rectangle rectangle = new Rectangle();
        BufferedImage bi = ImageIO.read(new File("blur.png"));

        BufferedImage biout = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = biout.getGraphics();
    }

    @Test
    public void t2() throws Exception {
        File file = new File("blur.png");
        ImageCutterUtil.cutImage(
                file, new File("temp.png"), new Rectangle(100, 100, 100, 100));
    }

    @Test
    public void t3() {
        ImageCutTask imageCutTask = new ImageCutTask(new File("blur.png"),
                new File("./out").toPath(),
                new Rectangle(new Point(100, 100), new Dimension(100, 100)));
        imageCutTask.run();
    }
}
