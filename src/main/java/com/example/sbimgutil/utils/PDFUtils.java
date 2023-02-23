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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class PDFUtils {

    private PDFUtils(){

    }

    public static void mergeIntoPdf(Collection<File> imgFiles,File cataFile, OutputStream os) throws Exception {

        PdfWriter pdfWriter = new PdfWriter(os);
        PdfDocument doc = new PdfDocument(pdfWriter);
        PdfOutline rootOutLines = doc.getOutlines(false);

        if(cataFile!=null && cataFile.exists()) {
            Bookmark rootBookMark = parsePdfCatagory(cataFile);
            addCata(rootOutLines, rootBookMark);
        }
        for (File file : imgFiles) {
            System.out.println("add page:"+file.getAbsolutePath());
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
        pdfOutlineNextLevel.addDestination(PdfExplicitDestination.createFit(bookmark.getPage()));
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
            bookmark.order=i;
            Bookmark parent;
            int gap = bookmark.level.compareTo(previous.level);
            if (gap>0) {
                parent=previous;
            } else if (gap==0) {
                parent=previous.parent;
            } else {
                parent=previous.parent.parent;
            }
            bookmark.setParent(parent);
            parent.addChild(bookmark);
        }
        return bookmarks.getFirst();
    }
}
/**
 * @desc 定义书签
 */
@NoArgsConstructor
@Data
class Bookmark implements Comparable<Bookmark> {
    Integer page;
    String title;
    Integer level;
    Bookmark parent;
    Integer order;

    Set<Bookmark> children = new TreeSet<>();

    public Bookmark(int level, String title, String page) {
        this.level = level;
        this.title = title;
        this.page = Integer.valueOf(page);
    }

    @Override
    public int compareTo(Bookmark o) {
        if(this.page.equals(o.page)) {
            return this.order.compareTo(o.order);
        }
        else
            return this.page.compareTo(o.page);
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                ", page=" + page +
                ", title='" + title + '\'' +
                ", level=" + level +
//                ", parent=" + parent +
//                ", children=" + children +
                '}';
    }

    public void addChild(Bookmark bookmark) {
        children.add(bookmark);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bookmark bookmark = (Bookmark) o;

        if (!page.equals(bookmark.page)) return false;
        if (!title.equals(bookmark.title)) return false;
        return level.equals(bookmark.level);
    }

    @Override
    public int hashCode() {
        int result = page.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + level.hashCode();
        return result;
    }
}