package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.text.pdf.PdfDictionary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
@Slf4j
public class PDFUtils {

    private PDFUtils(){

    }

    public static void addCata(File pdfFile, File cataFile, File outFile) throws Exception {

        FileUtil.mkParentDirs(outFile);
        com.itextpdf.kernel.pdf.PdfReader reader = new com.itextpdf.kernel.pdf.PdfReader(pdfFile);
        PdfDocument inDoc = new PdfDocument(reader);
        PdfWriter writer = new PdfWriter(outFile);
        PdfDocument outDoc = new PdfDocument(new PdfWriter(outFile));

        for (int i = 1; i <= inDoc.getNumberOfPages(); i++) {
            inDoc.copyPagesTo(i, i, outDoc);
        }
        PdfOutline outlines = outDoc.getOutlines(false);
        PdfBookmark bookmark = CataParser.parseTxt(cataFile);
        CataParser.addCata(outlines, bookmark);
        outDoc.close();
        inDoc.close();
        reader.close();
        writer.close();
    }


    public static void split(File pdfFile, Path outPath) throws IOException {
            PdfReader pdfReader = new PdfReader(pdfFile.getAbsolutePath());
            PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
            RenderListener listener = new RenderListener() {
                final AtomicInteger imageNum = new AtomicInteger(0);
                @Override
                public void renderImage(ImageRenderInfo renderInfo) {
                    try {
//                        PdfDictionary imageDictionary = renderInfo.getImage().getDictionary();
//                        int width = imageDictionary.getAsNumber(com.itextpdf.text.pdf.PdfName.WIDTH).intValue();
//                        int height = imageDictionary.getAsNumber(com.itextpdf.text.pdf.PdfName.HEIGHT).intValue();
                        BufferedImage image = renderInfo.getImage().getBufferedImage();
                        File outImgFile = outPath.resolve(imageNum.incrementAndGet() + ".jpg").toFile();
                        FileUtil.mkParentDirs(outImgFile);
                        ImageIO.write(image, "jpg", outImgFile);
                    } catch (IOException e) {
                        log.error("文件无法解析"+renderInfo.getRef(),e);
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void endTextBlock() {
                }

                @Override
                public void beginTextBlock() {
                }

                @Override
                public void renderText(TextRenderInfo renderInfo) {
                }
            };
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                parser.processContent(i, listener);
            }
            pdfReader.close();
            log.debug("PDF successfully converted to images!");
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

    public static void createOutXmlbyLabels(File outFile,List<Label> labels) throws IOException, ImageReadException {

        FileUtil.mkParentDirs(outFile);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        for (var label : labels) {
            try {
//                Dimension imageSize = Imaging.getImageSize(label.getMarkedImageFile());
                ImageData imageData = ImageDataFactory.create(label.getMarkedImageFile().getAbsolutePath());
                float width = imageData.getWidth();
                float height = imageData.getHeight();
                sb.append(
                        "\t<page width=\"%s\"  height=\"%s\" pageName=\"%s\">\n"
                                .formatted(width, height, label.getMarkedImageFile().getName())
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
