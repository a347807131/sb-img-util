package fun.gatsby.sbimgutil;

import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.utils.ImageUtils;
//import com.itextpdf.text.BadElementException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfWriter;
import fun.gatsby.sbimgutil.utils.PicCompressUtils;
import lombok.extern.slf4j.Slf4j;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class DemoTest {

    @Test
    public void pd1() {
        String s = "123123";
        boolean matches = s.matches("123123");
        System.out.println();
    }

    @Test
    public void test() throws IOException {
        int compressLimit = 500;
        File dir = new File("C:\\Users\\Gatsby\\datasets/图片处理模板/samples");
        List<String> fileNames = List.of(
//                "67"//e0.16
//                "79",//0.125
                "83", //e 0.125
                "85", //0.125
                "98", //0.111
                "103" //e0.11
//                "142" ,//e=0.075
//                "164" //e0.0625
        );
        float quality = 0.5f;
        float encoding = 0.5f;
        for (String fileName : fileNames) {
            File oriTifFile = new File(dir, fileName + ".tiff");
            File outFile = new File(oriTifFile.getParentFile(), oriTifFile.getName() + ".jp2");
            BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);
            float fsize = oriTifFile.length() / (1024f * 1024);
            float size = oriTifFile.length() / (1024f * 1024);
            encoding = -0.001f * fsize + 0.227f;
            while (size > 0.5f || size < 0.4f) {
//                bufferedImageToSave = ImageIO.read(outFile);
                PicCompressUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), quality, encoding);
                size = outFile.length() / (1024 * 1024f);
                log.info("输出文件大小{}m,原文件大小{}m", size, oriTifFile.length() / 1024);
                System.out.println(encoding);
                if (size > 0.5f)
                    encoding = -encoding / 10 + encoding;
                else
                    encoding = encoding / 10 + encoding;
            }
            System.out.println("#########################");
        }

    }

    @Test
    public void test1() throws IOException {
        float quality = 0.5f;
        File dir = new File("C:\\Users\\Gatsby\\datasets/图片处理模板/samples");
        List<String> fileNames = List.of(
//                "67"//e0.16
//                "79",//0.125
                "83", //e 0.125
                "85", //0.125
                "98", //0.111
                "103" //e0.11
//                "142" ,//e=0.075
//                "164" //e0.0625
        );

        File oriTifFile = new File("/Users/gatsby/IdeaProjects/sb-img-util/src/main/resources/demo.tif");
        File outFile = new File( oriTifFile.getName() + ".jp2");

        BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);
        float fsize = oriTifFile.length() / (1024f * 1024);
        float encoding = -0.001f * fsize + 0.227f;
        PicCompressUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), quality, encoding);
        log.info("输出文件大小{}m,原文件大小{}m", outFile.length() / 1024, oriTifFile.length() / 1024);
    }


    @Test
    public void test2(){
        String s="     五行本體受病相傳為病     第五章 18";
        String[] splits = s.split(" ");
        boolean matches = splits[splits.length - 1].matches("^[0-9]*");
        System.out.println();

        boolean matches1 = s.matches(" ^((?!第五章).)*$");
        boolean matches2 = s.matches("^(?!seka).*$");

        boolean matches3 = s.matches(".*");
        System.out.println();
    }

    @Test
    public void test3() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(2200);
        long between = LocalDateTimeUtil.between(now, LocalDateTime.now(), ChronoUnit.SECONDS);
        System.out.println("任务执行时间：" + between + "秒");
    }

    @Test
    public void test4() throws IOException {
        String file = this.getClass().getResource("/demo.tif").getFile();
        File file1 = new File(file);

        BufferedImage bufferedImage = ImageIO.read(new File(file));
        ImageIO.write(bufferedImage, "jpeg2000", new File("demo.jp2"));
//        ImageIO.getImageWriters(file1, "jp2");
    }

    @Test
    public void test5() throws IOException {
        Desktop.getDesktop().open(new File("guide.pdf"));
    }
}