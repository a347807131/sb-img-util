package com.example.sbimgutil.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PDFUtils {

    private PDFUtils(){

    }

    public static void mergeIntoPdf(Collection<File> imgFiles,File cataFile, OutputStream os) throws Exception {

        if(imgFiles==null || imgFiles.isEmpty()) {
            log.warn("没有图片文件无法进行合并{}",cataFile);
            return;
        }
        PdfWriter pdfWriter = new PdfWriter(os);
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
            try {
                imageData = ImageDataFactory.create(file.getAbsolutePath());
            }catch (IOException e) {
                throw new IOException("图片文件无法解析"+file,e);
            }
            PdfPage page = doc.addNewPage(new PageSize(imageData.getWidth(), imageData.getHeight()));
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.addImage(imageData, 0, 0, false);
        }
        doc.close();
    }

    private static void addCata(PdfOutline pdfOutline, PdfBookmark bookmark) {
        if(bookmark==null) return;
        PdfOutline pdfOutlineNextLevel = pdfOutline.addOutline(bookmark.getTitle());
        pdfOutlineNextLevel.addDestination(PdfExplicitDestination.createFit(bookmark.getPage()));
        for (PdfBookmark child : bookmark.getChildren()) {
            addCata(pdfOutlineNextLevel,child);
        }
    }

    public static void mergeIntoPdf(Collection<File> imgFiles, OutputStream os) throws Exception {
        mergeIntoPdf(imgFiles,null,os);
    }
}
