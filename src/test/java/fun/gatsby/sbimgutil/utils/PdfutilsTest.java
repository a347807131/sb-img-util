package fun.gatsby.sbimgutil.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.TreeSet;

@Slf4j
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
    public void testParseCataTxtFile()  {

        URL resource = this.getClass().getResource("/");
        LinkedList<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, new File(resource.getPath(),
                        "5 目录")
                , f -> f.getName().startsWith("0001") || (f.isDirectory() && f.getName().endsWith("154"))
        );
        for (File f : files) {
            System.out.println(f);
            PdfBookmark bookmark = null;
            try {
                bookmark = CataParser.parseTxt(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.debug("this :{} parent:{}",bookmark,bookmark.parent);
        }
    }

    @Test
    public void testTail() {
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

    @Test
    public void testExtractToc() throws IOException {
        PdfReader pdfReader = new PdfReader("C:\\Users\\Gatsby\\Downloads/小镇喧嚣.pdf");
        PdfDocument pdfDocument = new PdfDocument(pdfReader);
        PdfOutline outlines = pdfDocument.getOutlines(false);
        for (PdfOutline outline : outlines.getAllChildren()) {
            System.out.println(outline.getTitle());
        }
    }

}
