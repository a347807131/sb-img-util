package fun.gatsby.sbimgutil.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.Deflater;

@Slf4j
public class PDFUtils {

    private PDFUtils(){

    }

    /**
     *
     * @param imgFiles 图片文件
     * @param cataFile 目录文件
     * @param os 输出流
     * @param width 图片宽度 ,0 表示不缩放
     * @throws Exception
     */
    public static void mergeIntoPdf(Collection<File> imgFiles,File cataFile,OutputStream os) throws Exception {

        if(imgFiles==null || imgFiles.isEmpty()) {
            log.warn("没有图片文件无法进行合并{}",cataFile);
            return;
        }
        PdfWriter pdfWriter = new PdfWriter(os);
        pdfWriter.setCompressionLevel(Deflater.BEST_COMPRESSION);
        PdfDocument doc = new PdfDocument(pdfWriter);
        PdfOutline rootOutLines = doc.getOutlines(false);

        if(cataFile!=null && cataFile.exists()) {
            PdfBookmark rootBookMark = CataParser.parseTxt(cataFile);
            addCata(rootOutLines, rootBookMark);
        }else {
            log.debug("目录文件{}不存在或空，不作添加目录处理",cataFile);
        }
        for (File file : imgFiles) {
            ImageData imageData;
            Path tempFile = Files.createTempFile("sb-img-util", file.hashCode() + "jpg");
            try {
                BufferedImage bf = ImageIO.read(file);
                float scale =  960f / bf.getWidth();
                BufferedImage scaledBf = ImageUtils.scale(bf, scale);
                ImageIO.write(scaledBf, "jpeg", tempFile.toFile());
                imageData = ImageDataFactory.create(tempFile.toFile().getAbsolutePath());
            }catch (IOException e) {
                throw new IOException("图片文件无法解析"+file,e);
            }finally {
                tempFile.toFile().delete();
            }
            PdfPage page = doc.addNewPage(new PageSize(imageData.getWidth(),imageData.getHeight()));
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.addImage(imageData, 0, 0, false);
        }
        doc.close();
        pdfWriter.close();
    }

    private static void addCata(PdfOutline pdfOutline, PdfBookmark bookmark) {
        if(bookmark==null) return;
        PdfOutline pdfOutlineNextLevel = pdfOutline.addOutline(bookmark.getTitle());
        pdfOutlineNextLevel.addDestination(PdfExplicitDestination.createFit(bookmark.getPage()));
        for (PdfBookmark child : bookmark.getChildrens()) {
            addCata(pdfOutlineNextLevel, child);
        }
    }

    public static void mergeIntoPdf(Collection<File> imgFiles, OutputStream os) throws Exception {
        mergeIntoPdf(imgFiles,null,os);
    }
}
