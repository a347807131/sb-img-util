package com.example.sbimgutil;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
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

public class DemoTest{
    String tifFilePath =
            "C:\\Users\\Gatsby\\IdeaProjects\\sb-img-util\\src\\main\\resources\\" +"08012194802220100.tif";
    //                    "08012194802220100.tif";
    File oriFile = new File(tifFilePath);
    @Test
    public void t3() throws IOException {
        BufferedImage bufferdImage= ImageIO.read(new File(tifFilePath));
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
        param.setFilter(J2KImageWriteParam.FILTER_53 );
        writer.write(null, ioimage, param);
        System.out.println(System.currentTimeMillis() - s);
        writer.dispose();
        ios.flush();
        ios.close();
        bufferdImage.flush();
    }

    @Test
    public void pdftest() throws IOException {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource("C:\\Users\\Gatsby\\IdeaProjects\\sb-img-util\\src\\main\\resources\\blur.png");
        pdfMergerUtility.setDestinationFileName("demo.pdf");
        pdfMergerUtility.mergeDocuments();
    }
}
