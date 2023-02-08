package com.example.sbimgutil;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import lombok.experimental.StandardException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class TiffUtils {

    public static void TransformTifToJpg(File oriTifFile, File jpgOutFile, int limit) {

//        String outPath = oriFile.getParentFile()+ "/out.jpg";
//        BufferedImage bufferdImage= ImageIO.read(new File(tifFilePath));
//        // 把图片读入到内存中
//        byte[] bytes = PicUtils.compressPicForScale(TiffUtils.ImageToBytes(bufferdImage),500);
//
//        bufferdImage= TiffUtils.bytesToImage(bytes);
//
//        ImageIO.write(bufferdImage,"jpg 2000",new File(outPath));
//
//        FileUtils.writeByteArrayToFile(new File(outPath),bytes);

    }
    public static void TramsforTifToJp2(File originTifFile,File outFile,int limit){


        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG2000");
        ImageWriter writer = writers.next();
        long s = System.currentTimeMillis();
        try (
            ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);
        ){
            BufferedImage originTifBuferedImage = ImageIO.read(originTifFile);
            writer.setOutput(ios);
        }catch (Exception e){
            throw new RuntimeException();
        }
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
    public static void compressTifToJpg(){


    }
    public static void mergeJpgsToPdf(){

    }
    public static byte[] ImageToBytes(BufferedImage bufferedImage){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", out);
        } catch (IOException e) {
            //log.error(e.getMessage());
        }
        return out.toByteArray();
    }
    public static BufferedImage bytesToImage(byte[] bytes){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            BufferedImage outImage = ImageIO.read(byteArrayInputStream);
            return outImage;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
//    public void compressor(String inputFile, String outputFile) throws IOException {
//        J2KImageWriteParam iwp = new J2KImageWriteParam();
//        FileInputStream fis = new FileInputStream(new File(inputFile));
//        BufferedImage image = ImageIO.read(fis);
//        fis.close();
//
//        if (image == null)
//        {
//            System.out.println("If no registered ImageReader claims to be able to read the resulting stream");
//        }
//
//        Iterator writers = ImageIO.getImageWritersByFormatName("JPEG2000");
//        String name = null;
//        ImageWriter writer = null;
//        while (name != "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriter") {
//            writer = (ImageWriter) writers.next();
//            name = writer.getClass().getName();
//            System.out.println(name);
//        }
//        File f = new File(outputFile);
//        long s = System.currentTimeMillis();
//        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
//        writer.setOutput(ios);
//        J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
//        IIOImage ioimage = new IIOImage(image, null, null);
//        param.setSOP(true);
//        param.setWriteCodeStreamOnly(true);
//        param.setProgressionType("layer");
//        param.setLossless(false);
//        param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
//        param.setCompressionType("JPEG2000");
//        param.setCompressionQuality(0.1f);
//        param.setEncodingRate(1.01);
//        param.setFilter(J2KImageWriteParam.FILTER_97);
//
//
//        writer.write(null, ioimage, param);
//        System.out.println(System.currentTimeMillis() - s);
//        writer.dispose();
//        ios.flush();
//        ios.close();
//
//        image.flush();
//    }
}
