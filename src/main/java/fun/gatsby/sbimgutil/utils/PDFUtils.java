package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import fun.gatsby.lang.tuple.Tuple3;
import lombok.experimental.PackagePrivate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

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
            CataParser.addCata(rootOutLines, rootBookMark);
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

    public static void mergeIntoPdf(Collection<File> imgFiles, OutputStream os) throws Exception {
        mergeIntoPdf(imgFiles,null,os);
    }

    public static void createOutXmlbyLabels(File outFile,LinkedList<Label> labels) throws IOException, ImageReadException {

        FileUtil.mkParentDirs(outFile);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        for (var label : labels) {
            try {
                ImageInfo imageInfo = Imaging.getImageInfo(label.getMarkedImageFile());
                sb.append(
                        "\t<page width=\"%s\"  height=\"%s\" pageName=\"%s\">\n"
                                .formatted(imageInfo.getWidth(), imageInfo.getHeight(), label.getMarkedImageFile().getName())
                );
            }catch (Exception e) {
                log.warn("图片文件无法解析"+label.getMarkedImageFile()+ e);
                continue;
            }
            for (Label.Detection detection : label.getDetections()) {
                int[][] points = detection.getPoints();
                String pointsStr = Arrays.deepToString(points);
                sb.append("\t\t<detection points=\"%s\">\n".formatted(pointsStr));
                    sb.append("\t\t\t%s\n".formatted(detection.getTranscription()));
                sb.append("\t\t</detection>\n");
            }
            sb.append("\t</page>\n");
        }
        sb.append("</xml>");

        try (OutputStream os = Files.newOutputStream(outFile.toPath())){
            os.write(sb.toString().getBytes());
        }
    }
}
