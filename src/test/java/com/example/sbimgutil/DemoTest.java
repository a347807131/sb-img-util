package com.example.sbimgutil;

import com.example.sbimgutil.utils.TifUtils;
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
import java.nio.file.Files;
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
                "229.tiff","732.tif"
        );
        for (String fileName : fileNames) {
            File oriTifFile = new File(dir, fileName );
            File outFile = new File(oriTifFile.getParentFile(), oriTifFile.getName() + ".jp2");
            if(outFile.exists())
                outFile.delete();
            OutputStream os = Files.newOutputStream(outFile.toPath());
            BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);
            float fsize = oriTifFile.length() / (1024f * 1024);
            // FIXME: 2/27/2023 该公式需要重新计算
            float encoding = (float) (5.842e-6 * Math.pow(fsize, 2) - 0.002235 * fsize + 0.2732);
            float limitM = compressLimit / 1024f;
            if (limitM == 0) {
                long s = System.currentTimeMillis();
                TifUtils.transformImgToJp2(bufferedImageToSave,os);
                log.debug("转换jp2无损耗时{}s,文件名{}", (System.currentTimeMillis() - s)/1000f, oriTifFile.getAbsolutePath());
                return;
            }
            int compressTime = 1;
            long oriFileSizeM = oriTifFile.length() / (1024 * 1024);
            while (fsize > limitM || fsize < limitM * 0.8) {
                long s = System.currentTimeMillis();
                transformImgToJp2(bufferedImageToSave, os, 0.5f, encoding);
                fsize = outFile.length() / (1024 * 1024f);
                log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},耗时{}s,文件名{}", compressTime,
                        fsize,oriFileSizeM , encoding,
                        (System.currentTimeMillis() - s)/1000f,
                        oriTifFile.getAbsolutePath()
                );
                compressTime += 1;
                if (fsize > limitM)
                    encoding *=0.9;
                else if (fsize < limitM * 0.8)
                    encoding *= 1.1;
                else break;
                System.gc();
            }
            os.close();
        }
    }

    @Test
    public void test1() throws IOException {
        int compressLimit = 500;
        File dir =  new File("C:\\Users\\Gatsby\\datasets/图片处理模板/samples");
        List<String> fileNames = List.of(
                "229.tiff","732.tif"
        );
        float quality=0.5f;float encoding=0.5f;
        for (String fileName : fileNames) {
            long s = System.currentTimeMillis();
            File oriTifFile = new File(dir, fileName);
            File outFile = new File(oriTifFile.getParentFile(), oriTifFile.getName() + ".jp2");
            BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);

            OutputStream os = Files.newOutputStream(outFile.toPath());
            float limitM = compressLimit / 1024f;
            float fsize = oriTifFile.length() / (1024f * 1024);
            encoding = 0.2f;
            long oriFileSizeM = oriTifFile.length() / (1024 * 1024);
            while (fsize > 0.5f || fsize < 0.4f) {
                int compressTime = 1;
                while (fsize > limitM || fsize < limitM * 0.8) {
                    TifUtils.transformImgToJp2(bufferedImageToSave, os, 0.5f, encoding);
                    fsize = outFile.length() / (1024 * 1024f);
                    log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},耗时{}s,文件名{}", compressTime,
                            fsize, oriFileSizeM, encoding,
                            (System.currentTimeMillis() - s) / 1000f,
                            oriTifFile.getAbsolutePath()
                    );
                    compressTime += 1;
                    if (fsize > limitM)
                        encoding *= 0.9;
                    else if (fsize < limitM * 0.8)
                        encoding *= 1.1;
                    else break;
                    System.gc();
                }
            }
        }
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

    @Test
    public void test3() throws IOException {
        BufferedImage read = ImageIO.read(new File("C:\\Users\\Gatsby\\datasets\\图片处理模板\\1 扫描原图（TIFF格式）\\27030166 - Copy\\0001/0001.tif"));
    }
}