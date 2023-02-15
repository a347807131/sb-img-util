package com.example.sbimgutil.utils;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.util.List;
import java.util.Objects;

@Slf4j
public class TifUtils {

    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage){
        Point point = new Point((oriBufferedImage.getWidth() - blurBufferedImage.getWidth()) / 2, (oriBufferedImage.getHeight() - blurBufferedImage.getHeight()) / 2);
        Graphics g = oriBufferedImage.getGraphics();
        g.drawImage(blurBufferedImage, point.x, point.y, null);
        g.dispose();

    }

    public static void transformImgToJpg(BufferedImage bufferedImage, OutputStream outputStream, int limit) throws IOException {
        long s = System.currentTimeMillis();
        byte[] bytes =imageToBytes(bufferedImage);
        // 把图片读入到内存中
        if(limit>0){
            bytes = PicCompressUtils.compressPicForScale(bytes,limit);
        }
        IOUtils.write(bytes,outputStream);

        log.debug("转化为jpg并输出共耗时{}s",(System.currentTimeMillis()-s)/1000f);
    }

    public static void transformImgToJp2(BufferedImage bufferedImage, OutputStream outputStream, int limit){

        long s = System.currentTimeMillis();
        if(limit>0){
            byte[] bytesOrigin = imageToBytes(bufferedImage);
            byte[] bytes = PicCompressUtils.compressPicForScale(bytesOrigin,limit);
            log.debug("压缩前后大小对比{}k:{}k",bytesOrigin.length/1024,bytes.length/1024);
            bufferedImage = bytesToImage(bytes);
        }

        try (
                ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            ){
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
            param.setSOP(true);
            param.setWriteCodeStreamOnly(true);
            param.setProgressionType("layer");
            param.setLossless(true);
            param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");
//        param.setCompressionQuality(0.01f);
//            param.setEncodingRate(1.01);
            param.setFilter(J2KImageWriteParam.FILTER_53);
            writer.write(null, ioimage, param);
            writer.dispose();
            ios.flush();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        log.debug("压缩前大小，压缩后大小");
        log.debug("转化为jp2，是否压缩:{}，并输出共耗时{}s",limit>0? "是":"否",(System.currentTimeMillis()-s)/1000f);
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

    public static void mergeImgToPdf(List<File> imgFiles,OutputStream os) {
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(PageSize.A4, 0, 0, 0, 0); //new一个pdf文档
        try {
            PdfWriter.getInstance(doc, os); //pdf写入
            doc.open();//打开文档
            for (File imgFile : imgFiles) {  //循环图片List，将图片加入到pdf中
                doc.newPage();  //在pdf创建一页
                Image img = null; //通过文件路径获取image
                try {
                    img = Image.getInstance(imgFile.getAbsolutePath());
                } catch (BadElementException e) {
                    throw new RuntimeException(e);
                }
                float heigth = img.getHeight();
                float width = img.getWidth();
                int percent =
                    getPercent2(heigth, width);
                img.setAlignment(Image.MIDDLE);
                //试出来的
                img.scalePercent(14);// 表示是原来图像的比例;
                doc.add(img);
            }
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPercent(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        if (h > w) {
            p2 = 297 / h * 100;
        } else {
            p2 = 210 / w * 100;
        }
        p = Math.round(p2);
        return p;
    }

    public static int getPercent2(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        p2 = 530 / w * 100;
        p = Math.round(p2);
        return p;
    }
}
