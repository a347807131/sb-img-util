package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.itextpdf.kernel.pdf.PdfName.DeviceGray;
import static com.itextpdf.kernel.pdf.PdfName.ca;

/**
 * @author 张治忠
 */
@Slf4j
public class ImagesConverter {

    private static final PdfFont  baseFont;

    static {
        try {
            baseFont = PdfFontFactory.createFont("font/simhei.ttf", PdfEncodings.IDENTITY_H, false);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private final LinkedList<Label> labels;
    private final File cataFile;


    public ImagesConverter(LinkedList<Label> labels,File cataFile) {
        this.labels=labels;
        this.cataFile=cataFile;
    }


    @Data
    public static class OcrItemResult {

        /**
         * 识别的文本
         */
        private String transcription;

        private float[][] points;


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RectInfo {

        private float width;

        private float height;

        private float newWidth;

        private float newHeight;

        private float rate;

        private Image image;
    }

    /**
     * 文件ocr识别，后面自行调用接口实现
     * @return
     */
    public List<OcrItemResult> imageOcr(Label label) {
        List<Label.Detection> detections = label.getDetections();
        List<OcrItemResult> ocrItemResults = new ArrayList<>();
        for (Label.Detection detection : detections) {
            OcrItemResult ocrItemResult = new OcrItemResult();
            ocrItemResult.setTranscription(detection.getTranscription());

            int[][] points = detection.getPoints();
            float[][] newPoints = new float[points.length][points[0].length];
            for (int i = 0; i < points.length; i++) {
                for (int j = 0; j < points[i].length; j++) {
                    newPoints[i][j] = points[i][j];
                }
            }
            ocrItemResult.setPoints(newPoints);
            ocrItemResults.add(ocrItemResult);
        }
        return ocrItemResults;
    }

    /**
     * 得到图片的尺寸、缩放比例等信息
     * @return
     * @throws Exception
     */
    private RectInfo getRectInfo(Label label) throws Exception {
        Image image = new Image(ImageDataFactory.create(label.getMarkedImageFile().getAbsolutePath()));
        float width = image.getImageWidth();
        float height = image.getImageHeight();
        float rate = width / height;
        float newWidth;
        float newHeight;
        if (width > height) {
            newWidth = 595 * rate;
            newHeight = 595;
        } else {
            newWidth = 595;
            newHeight = 595 / rate;
        }
        image.scaleAbsolute(newWidth, newHeight);
        return new RectInfo(width, height, newWidth, newHeight, newWidth / width, image);
    }

    /**
     * 转换成双层pdf
     * @throws Exception
     */
    public void convertToBilayerPdf(File outFile) throws Exception {
        if (CollUtil.isEmpty(labels)) {
            return;
        }
        RectInfo firstRect = this.getRectInfo(labels.getFirst());
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        PdfWriter pdfWriter = new PdfWriter(fileOutputStream);
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        PageSize pageSize = new PageSize(new Rectangle(firstRect.getNewWidth(), firstRect.getNewHeight()));
        Document doc = new Document(pdfDoc, pageSize);
        doc.setMargins(0,0,0,0);
        for (int i = 0; i < labels.size(); i++) {
            Label label = labels.get(i);
            File imageFile = label.getMarkedImageFile();
            RectInfo rectInfo = this.getRectInfo(label);
            this.insertTextBoxes(label, doc, i + 1, rectInfo);
            doc.add(rectInfo.getImage());
            pdfDoc.addNewPage(new PageSize(new Rectangle(rectInfo.getNewWidth(), rectInfo.getNewHeight())));

        }

        if(cataFile!=null && cataFile.exists()) {
            PdfOutline outlines = pdfDoc.getOutlines(false);
            CataParser cataParser = new CataParser(cataFile);
            cataParser.parse(outlines);
        }else {
            log.debug("目录文件{}不存在或空，不作添加目录处理",cataFile);
        }

        doc.close();
        pdfWriter.close();
        IoUtil.close(fileOutputStream);
    }

    /**
     * 插入透明文字
     * @param rectInfo 尺寸信息
     * @throws Exception
     */
    private void insertTextBoxes(Label label,  Document doc, int pageNum,  RectInfo rectInfo) throws Exception {

        List<OcrItemResult> ocrItemResults = this.imageOcr(label);
        for (OcrItemResult ocrItemResult : ocrItemResults) {
            float rate = rectInfo.getRate();
            float[][] points = ocrItemResult.getPoints();
            for (int i = 0; i < points.length; i++) {
                for (int j = 0; j < points[i].length; j++) {
                    points[i][j] = points[i][j] * rate;
                }
            }
            float[] leftTopPoint = points[0];
            float[] rightTopPoint = points[1];
            float[] rightBottomPoint = points[2];
            float[] leftBottomPoint = points[3];
            float widthDiff = Math.max(rightTopPoint[0] - leftTopPoint[0], rightBottomPoint[0] - leftBottomPoint[0]);
            float heightDiff = Math.max(leftBottomPoint[1] - leftTopPoint[1], rightBottomPoint[1] - rightTopPoint[1]);
            float fontSize = Math.max(widthDiff, heightDiff) / (ocrItemResult.getTranscription().length()) - 0.1f;
            float height = rectInfo.getHeight() * rate;
            float diff = Math.max(widthDiff, heightDiff);

            Paragraph paragraph = new Paragraph();
            Text text = new Text(ocrItemResult.getTranscription());
            paragraph.add(text);
            paragraph.setFont(baseFont)
                    .setFontColor(Color.BLACK, 1f)
                    .setFontSize(fontSize)
                    .setFixedLeading(fontSize );

            if (widthDiff >= heightDiff) {
                double angleInRadians = Math.atan2(rightBottomPoint[1] - leftBottomPoint[1], rightBottomPoint[0] - leftBottomPoint[0]);
                paragraph.setFixedPosition(pageNum, leftBottomPoint[0], height - leftBottomPoint[1],  diff)
                        .setRotationAngle(angleInRadians);
            } else {
                double angleInRadians = Math.PI +  Math.atan2(leftBottomPoint[1] - leftTopPoint[1], leftBottomPoint[0] - leftTopPoint[0]);
                paragraph.setFixedPosition(pageNum, leftTopPoint[0], height - leftTopPoint[1],  diff)
                        .setRotationAngle(angleInRadians);
            }
            doc.add(paragraph);
        }
    }
}
