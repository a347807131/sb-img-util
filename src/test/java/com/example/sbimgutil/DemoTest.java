package com.example.sbimgutil;

import com.example.sbimgutil.utils.FileFetchUtils;
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
    public void test1() throws IOException {
        File dir =  new File("C:\\Users\\Gatsby\\datasets\\图片处理模板\\samples\\大小不符合");
        List<File> files =new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, dir);
        for (File oriTifFile : files) {
            File outFile = new File(dir.getParentFile(), oriTifFile.getName() + ".jp2");
            BufferedImage bufferedImageToSave = ImageIO.read(oriTifFile);
            TifUtils.transformImgToJp2(bufferedImageToSave, outFile, 500);
        }
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