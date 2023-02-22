package com.example.sbimgutil.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PdfUtils {

    private PdfUtils(){

    }

    public static void mergeIntoPdf(Collection<File> imgFiles,File cataFile, OutputStream os) throws Exception {

        PdfWriter pdfWriter = new PdfWriter(os);
        PdfDocument doc = new PdfDocument(pdfWriter);
        PdfOutline rootOutLines = doc.getOutlines(false);
        if(cataFile!=null) {
            Bookmark rootBookMark = parsePdfCatagory(cataFile);
            addCata(rootOutLines, rootBookMark);
        }
        for (File file : imgFiles) {
            ImageData imageData = ImageDataFactory.create(file.getAbsolutePath());
            PdfPage page = doc.addNewPage(new PageSize(imageData.getWidth(), imageData.getHeight()));
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.addImage(ImageDataFactory.create(file.getAbsolutePath()), 0, 0, false);
        }

        doc.close();
    }

    private static void addCata(PdfOutline pdfOutline, Bookmark bookmark) {
        if(bookmark==null) return;
        PdfOutline pdfOutlineNextLevel = pdfOutline.addOutline(bookmark.getTitle());
        pdfOutline.addDestination(PdfExplicitDestination.createFit(bookmark.getPage()));
        for (Bookmark child : bookmark.getChildren()) {
            addCata(pdfOutlineNextLevel,child);
        }
    }

    public static void mergeIntoPdf(Collection<File> imgFiles, OutputStream os) throws Exception {
        mergeIntoPdf(imgFiles,null,os);
    }

    public static Bookmark parsePdfCatagory(File cataTxtFile) throws IOException {
        List<String> lines = FileUtils.readLines(cataTxtFile, Charset.defaultCharset());

        String sepreator = "     ";

        LinkedList<Bookmark> bookmarks = new LinkedList<>();
        for (String line : lines) {
            if(StringUtils.isEmpty(line)) continue;
            line=line.stripTrailing();
            String[] split = line.split(sepreator);

            int level = split.length - 1;
            String title = split[split.length - 2];
            String page= split[split.length - 1];
            Bookmark bookmark = new Bookmark(level, title, page);
            bookmarks.add(bookmark);
        }

        for (int i = 1; i < bookmarks.size(); i++) {
            Bookmark bookmark = bookmarks.get(i);
            Bookmark previous = bookmarks.get(i - 1);
            Bookmark parent;
            if (bookmark.getLevel() > previous.getLevel()) {
                previous.addChild(bookmark);
                parent=previous;
            } else if (bookmark.getLevel().equals(previous.getLevel())) {
                previous.getParent().addChild(bookmark);
                parent=previous.getParent();
            } else {
                previous.parent.parent.addChild(bookmark);
                parent=previous.parent.parent;
            }
            bookmark.setParent(parent);
        }
        return bookmarks.getFirst();
    }
}
