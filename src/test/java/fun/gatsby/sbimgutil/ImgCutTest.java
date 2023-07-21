package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.utils.ImageCutterUtil;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImgCutTest {

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
    public void t4() throws IOException {
        List<String> strings = Files.readAllLines(Path.of("C:\\Users\\Gatsby\\IdeaProjects\\sb-img-util\\src\\test\\resources\\label.txt"));

        for (String string : strings) {
            fun.gatsby.sbimgutil.utils.Label label = Label.parse(Path.of("./"), string);
            System.out.println(label);
        }
    }


}
