package com.example.sbimgutil;

import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.TifUtils;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DemoTest{
    /** * 多图片合成pdf的限制后缀 */
    private static final List<String> IMAGE_SUFFIX = List.of("jpg", "png", "jpeg");
    /** * 多个图片合成一个pdf * * @param imgFolder 多图片的文件夹路径 例如:"D:\\image\\" * @param target 合并的图片路径 "D:\\image\\merge.pdf" * @throws IOException */
    public static void manyImageToOnePdf(String imgFolder, String target) throws IOException {
        PDDocument doc = new PDDocument();
        //创建一个空的pdf文件
        doc.save(target);
        PDPage page;
        PDImageXObject pdImage;
        PDPageContentStream contents;
        BufferedImage bufferedImage;
        String fileName;
        float w, h;
        String suffix;
        File tempFile;
        int index;
        File folder = new File(imgFolder);
        for (int i = 0; i < folder.listFiles().length; i++) {
            tempFile = folder.listFiles()[i];
            if (!tempFile.isFile()) {
                continue;
            }
            fileName = tempFile.getName();
            index = fileName.lastIndexOf(".");
            if (index == -1) {
                continue;
            }
            //获取文件的后缀
            suffix = fileName.substring(index + 1);
            //如果文件后缀不是图片格式,跳过当前循环
            if (!IMAGE_SUFFIX.contains(suffix)) {
                continue;
            }
            bufferedImage = ImageIO.read(folder.listFiles()[i]);
            //Retrieving the page
            pdImage = LosslessFactory.createFromImage(doc, bufferedImage);
            w = pdImage.getWidth();
            h = pdImage.getHeight();
            page = new PDPage(new PDRectangle(w, h));
            contents = new PDPageContentStream(doc, page);
            contents.drawImage(pdImage, 0, 0, w, h);
            System.out.println("Image inserted");
            contents.close();
            doc.addPage(page);
        }
        //保存pdf
        doc.save(target);
        //关闭pdf
        doc.close();
    }

    public static void main(String[] args) throws IOException {
        imageToPdf(new File("C:\\Users\\Gatsby\\datasets\\1948-02\\无损JPEG\\2000301\\0001\\08012194802220100.jpg"), new File("demo.pdf"));
    }

    public static void imageToPdf(File imageFile, File outFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);

        PDDocument outDocument = new PDDocument();

        PDImageXObject imageXObject = LosslessFactory.createFromImage(outDocument, image);
        PDPage pdPage = new PDPage(PDRectangle.A4);
        outDocument.addPage(pdPage);
        PDPageContentStream pageContentStream = new PDPageContentStream(outDocument, pdPage);
        float height = pdPage.getMediaBox().getHeight();//要将图片在pdf中绘制多高，这里宽度直接使用了pdfpage的宽度，即横向铺满，这里的height也是使用了pdfpage的高度。因此最终结果是铺满整个pdf页。
        float y = pdPage.getMediaBox().getHeight() - height;//这里比较奇葩的是这个坐标是以左下角为原点坐标的。
        pageContentStream.drawImage(imageXObject, 0, y, pdPage.getMediaBox().getWidth(), height);
        pageContentStream.close();

//        if (!outFile.getParentFile().exists()) {
//            outFile.getParentFile().mkdirs();
//        }

        outDocument.save(outFile);
        outDocument.close();
    }

    @Test
    public void  main() throws FileNotFoundException {
        File jpgDir = new File("C:\\Users\\Gatsby\\datasets\\1948-02\\无损JPEG\\2000302\\0001");
        File outFile = new File("demo.pdf");
        LinkedList<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files,jpgDir);
        TifUtils.mergeImgToPdf(files,new FileOutputStream(outFile));
    }

    @Test
    public void pd1(){
        String s = "123123";
        boolean matches = s.matches("123123");
        System.out.println();
    }
}
