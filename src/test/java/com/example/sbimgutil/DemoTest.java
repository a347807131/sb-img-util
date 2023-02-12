package com.example.sbimgutil;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

public class DemoTest {
    String tifFilePath =
            "C:\\Users\\Gatsby\\IdeaProjects\\sb-img-util\\src\\main\\resources\\" + "08012194802220100.tif";
    //                    "08012194802220100.tif";
    File oriFile = new File(tifFilePath);

    @Test
    public void t3() throws IOException {
        BufferedImage bufferdImage = ImageIO.read(new File(tifFilePath));
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG2000");
        ImageWriter writer = writers.next();
        File f = new File("demo.jp2");
        long s = System.currentTimeMillis();
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
        IIOImage ioimage = new IIOImage(bufferdImage, null, null);

        param.setSOP(true);
        param.setWriteCodeStreamOnly(true);
        param.setProgressionType("layer");
        param.setLossless(true);
        param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType("JPEG2000");
//        param.setCompressionQuality(0.01f);
        param.setEncodingRate(1.01);
        param.setFilter(J2KImageWriteParam.FILTER_53);
        writer.write(null, ioimage, param);
        System.out.println(System.currentTimeMillis() - s);
        writer.dispose();
        ios.flush();
        ios.close();
        bufferdImage.flush();
    }

    @Test
    public void pdfTest() throws IOException {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource("/Users/gatsby/IdeaProjects/sb-img-util/src/main/resources/blur.png");
        pdfMergerUtility.setDestinationFileName("demo.pdf");
        pdfMergerUtility.mergeDocuments();

    }



    public static void imageToPdf(File imageFile, File outFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        PDDocument outDocument = new PDDocument();
        PDImageXObject imageXObject = LosslessFactory.createFromImage(outDocument, image);
        PDPage pdPage = new PDPage(PDRectangle.A4);
        outDocument.addPage(pdPage);
        PDPageContentStream pageContentStream = new PDPageContentStream(outDocument, pdPage);
        float height = pdPage.getMediaBox().getHeight();//要将图片在pdf中绘制多高，这里宽度直接使用了pdfpage的宽度，即横向铺满，这里的height也是使用了pdfpage的高度。因此最终结果是铺满整个pdf页。
        float y = pdPage.getMediaBox().getHeight() - height;//这里比较奇葩的是这个坐标是以左下角为原点坐标的。
        pageContentStream.drawImage(imageXObject, 0, y, pdPage.getMediaBox().getWidth(), height);
        pageContentStream.close();
        outDocument.save(outFile);
        outDocument.close();
    }
}
