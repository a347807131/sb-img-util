package com.example.sbimgutil;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Slf4j
public class TifUtils {

    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage){
        Point point = new Point((oriBufferedImage.getWidth() - blurBufferedImage.getWidth()) / 2, (oriBufferedImage.getHeight() - blurBufferedImage.getHeight()) / 2);
        Graphics g = oriBufferedImage.getGraphics();
        g.drawImage(blurBufferedImage, point.x, point.y, null);
        g.dispose();

    }

    public static void TransformImgToJpg(BufferedImage bufferedImage, OutputStream outputStream, int limit) throws IOException {
        long s = System.currentTimeMillis();
        // 把图片读入到内存中
        byte[] bytes = PicCompressUtils. compressPicForScale(imageToBytes(bufferedImage),limit);
        IOUtils.write(bytes,outputStream);
        log.debug("转化为jpg并输出共耗时{}s",(System.currentTimeMillis()-s)/1000f);
    }

    public static void tranformImgToJp2(BufferedImage bufferedImage, OutputStream outputStream, int limit){

        long s = System.currentTimeMillis();
        if(limit>0){
            byte[] bytes = PicCompressUtils.compressPicForScale(imageToBytes(bufferedImage),limit);
            bufferedImage = bytesToImage(bytes);
        }

        try (
                ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            ){
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG2000");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);
            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            IIOImage ioimage = new IIOImage(bufferedImage, null, null);
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
            writer.dispose();
            ios.flush();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        log.debug("转化为jp2，是否压缩:{}，并输出共耗时{}s",limit>0? "是":"否",(System.currentTimeMillis()-s)/1000f);
    }

    public static void mergeJpgsToPdf(){

    }
    public static byte[] imageToBytes(BufferedImage bufferedImage){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
    public static BufferedImage bytesToImage(byte[] bytes){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
