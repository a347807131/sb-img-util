package com.example.sbimgutil.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.TreeSet;

public class PdfutilsTest {
    @Test
    public void testPdfMerge() throws Exception {
        String jpgDirpath="C:/Users/Gatsby/datasets/图片处理模板/3 水印有损JP2000/27030166/有水印/27030166/0001";
        String cataPath="C:/Users/Gatsby/datasets/图片处理模板/1 扫描原图（TIFF格式）/../5 目录/27030166/0001.txt";
        File jpgDir = new File(jpgDirpath);
        File outFile = new File("demo.pdf");
        File cataFile = new File(cataPath);
        LinkedList<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, jpgDir);
        PDFUtils.mergeIntoPdf(
                files,
                cataFile,
                new FileOutputStream(outFile)
        );
    }
    @Test
    public void testPdfMerge1() throws Exception {
        File jpgDir = new File("C:/Users/Gatsby/datasets/图片处理模板/3 水印有损JP2000/27030166/有水印/27030166/0001");
        File outFile = new File("demo.pdf");
        LinkedList<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, jpgDir);
        PDFUtils.mergeIntoPdf(
                files,
                new File("C:/Users/Gatsby/datasets/图片处理模板/4 PDF/无水印/27030166/0001.txt"),
                new FileOutputStream(outFile));
    }

    @Test
    public void testParseCataTxtFile() throws Exception {
        File file=new File("C:/Users/Gatsby/datasets/图片处理模板/4 PDF/无水印/27030166/0002.txt");
        Bookmark bookmarks = PDFUtils.parsePdfCatagory(file);
        System.out.println(bookmarks);
    }

    @Test
    public void testTail(){
                // Create a TreeSet and inserting elements
                TreeSet<Integer> s = new TreeSet<>();

                // Adding Element to SortedSet
                s.add(1);
                s.add(5);
                s.add(2);
                s.add(3);
                s.add(9);

                // Returning the set with elements
                // strictly less than the passed value
                System.out.print("Elements greater than or equal to 5 in set are : "
                        + s.tailSet(5));
            }

}
