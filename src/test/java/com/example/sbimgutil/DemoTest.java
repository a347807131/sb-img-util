package com.example.sbimgutil;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
//import com.itextpdf.text.BadElementException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

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
                transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), quality, encoding);
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

        File oriTifFile = new File(dir, "103" + ".tiff");
        File outFile = new File(oriTifFile.getParentFile(), oriTifFile.getName() + ".jp2");

        BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);
        float fsize = oriTifFile.length() / (1024f * 1024);
        float encoding = -0.001f * fsize + 0.227f;
        transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), quality, encoding);
        log.info("输出文件大小{}m,原文件大小{}m", outFile.length() / 1024, oriTifFile.length() / 1024);
    }

    public static void transformImgToJp2(BufferedImage bufferedImage, OutputStream outputStream, float quality, float encoding) {

        long s = System.currentTimeMillis();
        try (
                ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        ) {
            String name = null;
            ImageWriter writer = null;
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG2000");
            while (!Objects.equals(name, "com.github.jaiimageio.jpeg2000.impl.J2KImageWriter")) {
                writer = writers.next();
                name = writer.getClass().getName();
            }
            writer.setOutput(ios);
            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            IIOImage ioimage = new IIOImage(bufferedImage, null, null);
//            param.setSOP(true);
//            param.setWriteCodeStreamOnly(true);
//            param.setProgressionType("layer");
//            param.setLossless(true);
            param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");

            param.setCompressionQuality(quality);
            param.setEncodingRate(encoding);

            writer.write(null, ioimage, param);
            writer.dispose();
            ios.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("压缩前大小，压缩后大小");
        log.debug("转化为jp2，输出共耗时{}s", (System.currentTimeMillis() - s) / 1000f);
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

}