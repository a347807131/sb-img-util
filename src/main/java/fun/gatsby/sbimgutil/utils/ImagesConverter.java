package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.itextpdf.kernel.pdf.PdfName.DeviceGray;
import static com.itextpdf.kernel.pdf.PdfName.ca;
import static java.lang.Math.abs;

/**
 * @author 张治忠
 */
@Slf4j
public class ImagesConverter {

    private final PdfFont  baseFont =PdfFontFactory.createFont("font/simhei.ttf", PdfEncodings.IDENTITY_H, false);

    private final LinkedList<Label> labels;
    private final File cataFile;


    public ImagesConverter(LinkedList<Label> labels,File cataFile) throws IOException {
        this.labels=labels;
        this.cataFile=cataFile;
    }

    /**
     * 转换成双层pdf
     * @throws Exception
     */
    public void convertToBilayerPdf(File outFile) throws Exception {
        if (CollUtil.isEmpty(labels)) {
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        PdfWriter pdfWriter = new PdfWriter(fileOutputStream);
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        Document doc = new Document(pdfDoc);
        doc.setMargins(0,0,0,0);
        for (int i = 0; i < labels.size(); i++) {
            Label label = labels.get(i);
            File imageFile = label.getMarkedImageFile();
            // FIXME: 11/13/2023
            if(!imageFile.exists()) continue;
            ImageData imageData = ImageDataFactory.create(imageFile.getAbsolutePath());
            PdfPage pdfPage = pdfDoc.addNewPage(new PageSize(imageData.getWidth(), imageData.getHeight()));
            PdfCanvas canvas = new PdfCanvas(pdfPage);
            canvas.addImage(ImageDataFactory.create(imageFile.getAbsolutePath()), 0, 0, false);
            this.insertTextBoxes(label, doc, i + 1);
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
     * @throws Exception
     */
    private void insertTextBoxes1(Label label,  Document doc, int pageNum){

        for (Label.Detection detection : label.getDetections()) {
            Rectangle pageSize = doc.getPdfDocument().getPage(pageNum).getPageSize();
            int[][] points = detection.getPoints();
            Point point = new Point();
            var leftTopPoint = points[0];
            var rightTopPoint = points[1];
            var rightBottomPoint = points[2];
            var leftBottomPoint = points[3];
            float widthDiff = Math.max(rightTopPoint[0] - leftTopPoint[0], rightBottomPoint[0] - leftBottomPoint[0]);
            float heightDiff = Math.max(leftBottomPoint[1] - leftTopPoint[1], rightBottomPoint[1] - rightTopPoint[1]);
            float fontSize = Math.max(widthDiff, heightDiff) / (detection.getTranscription().length()) - 0.1f;
            float diff = Math.max(widthDiff, heightDiff);



            PdfPage pdfPage = doc.getPdfDocument().getPage(pageNum);

            var height=rightBottomPoint[1]-rightTopPoint[1];
            Text text = new Text(detection.getTranscription());
            Paragraph paragraph = new Paragraph();
            paragraph.add(text);
            paragraph.setFont(baseFont)
                    .setFontColor(Color.BLACK, 1f)
                    .setFontSize(fontSize)
                    .setFixedLeading(fontSize);
            paragraph.setFixedPosition(pageNum, leftTopPoint[0], pdfPage.getPageSize().getHeight()-leftTopPoint[1], diff);
//            if (widthDiff >= heightDiff) {
//                double angleInRadians = Math.atan2(rightBottomPoint[1] - leftBottomPoint[1], rightBottomPoint[0] - leftBottomPoint[0]);
//                paragraph.setFixedPosition(pageNum, leftBottomPoint[0], height - leftBottomPoint[1],  diff)
//                        .setRotationAngle(angleInRadians);
//            } else {
//                double angleInRadians = Math.PI +  Math.atan2(leftBottomPoint[1] - leftTopPoint[1], leftBottomPoint[0] - leftTopPoint[0]);
//                paragraph.setFixedPosition(pageNum, leftTopPoint[0], height - leftTopPoint[1],  diff)
//                        .setRotationAngle(angleInRadians);
//            }
            doc.add(paragraph);
        }
    }
    /**
     * 插入透明文字
     * @throws Exception
     */
    private void insertTextBoxes(Label label,  Document doc, int pageNum) {
        Rectangle pageSize = doc.getPdfDocument().getPage(pageNum).getPageSize();
        for (Label.Detection detection : label.getDetections()) {
            int[][] points = detection.getPoints();
            Point p0 = new Point(points[0][0], (int) (pageSize.getHeight() - points[0][1]));
            Point p1 = new Point(points[1][0], (int) (pageSize.getHeight() - points[1][1]));
            Point p2 = new Point(points[2][0], (int) (pageSize.getHeight() - points[2][1]));
            Point p3 = new Point(points[3][0], (int) (pageSize.getHeight() - points[3][1]));

            float widthDiff = Math.max(p1.x - p0.x, p2.x - p3.x);
            float heightDiff = Math.max( p0.y - p3.y,p1.y - p2.y);
            float fontSize = Math.max(widthDiff, heightDiff) / (detection.getTranscription().length()) - 0.1f;
            float diff = Math.max(widthDiff, heightDiff);

            Text text = new Text(detection.getTranscription());
            Paragraph paragraph = new Paragraph();
            paragraph.add(text);
            paragraph.setFont(baseFont)
                    .setFontColor(Color.BLACK, 1f)
                    .setFontSize(fontSize)
                    .setFixedLeading(fontSize);
            paragraph.setFixedPosition(pageNum, p0.x, p0.y, diff);
            paragraph.setRotationAngle(-Math.PI/2f);
//            if (widthDiff >= heightDiff) {
//                double angleInRadians = -Math.atan2(p2.y - p3.y, p0.x - p3.x);
//                paragraph.setRotationAngle(angleInRadians);
//            } else {
//                double angleInRadians = Math.PI -  Math.atan2(p3.y - p0.y,p3.x - p0.x);
//                paragraph.setRotationAngle(angleInRadians);
//            }
            doc.add(paragraph);
        }
    }
}
