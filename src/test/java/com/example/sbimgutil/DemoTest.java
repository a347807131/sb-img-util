package com.example.sbimgutil;

import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.PdfUtils;
import com.example.sbimgutil.utils.PicCompressUtils;
import com.example.sbimgutil.utils.TifUtils;
import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
//import com.itextpdf.text.BadElementException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class DemoTest {
    /**
     * 多图片合成pdf的限制后缀
     */
    private static final List<String> IMAGE_SUFFIX = List.of("jpg", "png", "jpeg");
//    /** * 多个图片合成一个pdf * * @param imgFolder 多图片的文件夹路径 例如:"D:\\image\\" * @param target 合并的图片路径 "D:\\image\\merge.pdf" * @throws IOException */
//    public static void manyImageToOnePdf(String imgFolder, String target) throws IOException {
//        PDDocument doc = new PDDocument();
//        //创建一个空的pdf文件
//        doc.save(target);
//        PDPage page;
//        PDImageXObject pdImage;
//        PDPageContentStream contents;
//        BufferedImage bufferedImage;
//        String fileName;
//        float w, h;
//        String suffix;
//        File tempFile;
//        int index;
//        File folder = new File(imgFolder);
//        for (int i = 0; i < folder.listFiles().length; i++) {
//            tempFile = folder.listFiles()[i];
//            if (!tempFile.isFile()) {
//                continue;
//            }
//            fileName = tempFile.getName();
//            index = fileName.lastIndexOf(".");
//            if (index == -1) {
//                continue;
//            }
//            //获取文件的后缀
//            suffix = fileName.substring(index + 1);
//            //如果文件后缀不是图片格式,跳过当前循环
//            if (!IMAGE_SUFFIX.contains(suffix)) {
//                continue;
//            }
//            bufferedImage = ImageIO.read(folder.listFiles()[i]);
//            //Retrieving the page
//            pdImage = LosslessFactory.createFromImage(doc, bufferedImage);
//            w = pdImage.getWidth();
//            h = pdImage.getHeight();
//            page = new PDPage(new PDRectangle(w, h));
//            contents = new PDPageContentStream(doc, page);
//            contents.drawImage(pdImage, 0, 0, w, h);
//            System.out.println("Image inserted");
//            contents.close();
//            doc.addPage(page);
//        }
//        //保存pdf
//        doc.save(target);
//        //关闭pdf
//        doc.close();
//    }
//
//    public static void main(String[] args) throws IOException {
//        imageToPdf(new File("C:\\Users\\Gatsby\\datasets\\1948-02\\无损JPEG\\2000301\\0001\\08012194802220100.jpg"), new File("demo.pdf"));
//    }
//
//    public static void imageToPdf(File imageFile, File outFile) throws IOException {
//        BufferedImage image = ImageIO.read(imageFile);
//
//        PDDocument outDocument = new PDDocument();
//
//        PDImageXObject imageXObject = LosslessFactory.createFromImage(outDocument, image);
//        PDPage pdPage = new PDPage(PDRectangle.A4);
//        outDocument.addPage(pdPage);
//        PDPageContentStream pageContentStream = new PDPageContentStream(outDocument, pdPage);
//        float height = pdPage.getMediaBox().getHeight();//要将图片在pdf中绘制多高，这里宽度直接使用了pdfpage的宽度，即横向铺满，这里的height也是使用了pdfpage的高度。因此最终结果是铺满整个pdf页。
//        float y = pdPage.getMediaBox().getHeight() - height;//这里比较奇葩的是这个坐标是以左下角为原点坐标的。
//        pageContentStream.drawImage(imageXObject, 0, y, pdPage.getMediaBox().getWidth(), height);
//        pageContentStream.close();
//
////        if (!outFile.getParentFile().exists()) {
////            outFile.getParentFile().mkdirs();
////        }
//
//        outDocument.save(outFile);
//        outDocument.close();
//    }

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
    public void addCata() {
        File pdfFile = new File("demo.pdf");
        File cataFile = new File("0001.txt");

        long s = System.currentTimeMillis();
        float fsize = 0.5f;
        for (int i = 0; i < 1000000000; i++) {
            float encoding = (float) (5.842e-06 * Math.pow(fsize, 2) - 0.002235 * fsize + 0.2732);
        }
        long c = System.currentTimeMillis() - s;
        log.info(String.valueOf((c - s) / (1000f)));
    }
}