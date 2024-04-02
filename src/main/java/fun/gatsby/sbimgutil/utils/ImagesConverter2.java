package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSON;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
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
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * @author 张治忠
 */
@Slf4j
public class ImagesConverter2 {

    private final PdfFont  baseFont ;
//            PdfFontFactory.createFont(FontConstants.TIMES_BOLD);
//            PdfFontFactory.createFont("font/simhei.ttf", PdfEncodings.IDENTITY_H, false);

    private final File cataFile;
    private final Map<File, FileOcrResultVO> imgFileToFileOcrResultMap;


    public ImagesConverter2(Map<File, FileOcrResultVO> imgFileToFileOcrResultMap, File cataFile) throws IOException {
        this.imgFileToFileOcrResultMap=imgFileToFileOcrResultMap;
        this.cataFile=cataFile;
        byte[] fontContents = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/font/simhei.ttf")));
        FontProgram fontProgram = FontProgramFactory.createFont(fontContents);
        baseFont=PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H, false);
    }

    public static ImagesConverter2 of(Path ocrJsonFilesDirPath,Path imgFilesDir, File cataFile) throws IOException {
        Map<File, FileOcrResultVO> imgFileToOcrResultFileMap= FileUtil.loopFiles(imgFilesDir.toFile()).stream()
                .collect(
                        LinkedHashMap::new,
                        (fileFileOcrResult, imageFile) -> {
                            String resultFileName = imageFile.getName()
                                    .substring(0, imageFile.getName().lastIndexOf(".") + 1)
                                    +"json"
                                    ;
                            File ocrJsonFile = ocrJsonFilesDirPath.resolve(resultFileName).toFile();
                            FileOcrResultVO fileOcrResultVO = JSON.parseObject(FileUtil.readUtf8String(ocrJsonFile), FileOcrResultVO.class);
                            fileFileOcrResult.put(imageFile, fileOcrResultVO);
                        },
                        LinkedHashMap::putAll
                );
        return new ImagesConverter2(imgFileToOcrResultFileMap,cataFile);
    }

    /**
     * 转换成双层pdf
     * @throws Exception
     */
    public void convertToBilayerPdf(File outFile) throws Exception {
        if (CollUtil.isEmpty(imgFileToFileOcrResultMap)) {
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        PdfWriter pdfWriter = new PdfWriter(fileOutputStream);
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        Document doc = new Document(pdfDoc);
        doc.setMargins(0,0,0,0);

        List<Map.Entry<File, FileOcrResultVO>> sortedEntries = imgFileToFileOcrResultMap.entrySet().stream().sorted((e1, e2) -> {
            String name1 = e1.getKey().getName();
            String name2 = e2.getKey().getName();
            return name1.compareTo(name2);
        }).toList();

        for (int i = 0; i < sortedEntries.size(); i++) {
            var entry=sortedEntries.get(i);
            File imgFile = entry.getKey();
            FileOcrResultVO fileOcrResultVO = entry.getValue();
            if(fileOcrResultVO ==null) continue;
            FileOcrResultVO.Page page = fileOcrResultVO.getPage();
            ImageData imageData = ImageDataFactory.create(imgFile.getAbsolutePath());
            PdfPage pdfPage = pdfDoc.addNewPage(new PageSize(imageData.getWidth(), imageData.getHeight()));
            PdfCanvas canvas = new PdfCanvas(pdfPage);
            canvas.addImage(ImageDataFactory.create(imgFile.getAbsolutePath()), 0, 0, false);
            this.insertTextBoxes(page, doc, i + 1);
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
     */

    private void insertTextBoxes(FileOcrResultVO.Page page, Document doc, int pageNum) {
        Rectangle pageSize = doc.getPdfDocument().getPage(pageNum).getPageSize();
        for (FileOcrResultVO.Page.Line line : page.getLines()) {
            int[] position = line.getPosition();
            Point ptl = new Point(position[0], position[1]);
            Point ptr = new Point(position[2], position[1]);
            Point pbr = new Point(position[2], position[3]);
            Point pbl = new Point(position[0], position[3]);

            Point p0 = new Point(ptl.x, (int) (pageSize.getHeight() - ptl.y));
            Point p1 = new Point(ptr.x, (int) (pageSize.getHeight() - ptr.y));
            Point p2 = new Point(pbr.x, (int) (pageSize.getHeight() - pbr.y));
            Point p3 = new Point(pbl.x, (int) (pageSize.getHeight() - pbl.y));

            float widthDiff = Math.max(p1.x - p0.x, p2.x - p3.x);
            float heightDiff = Math.max( p0.y - p3.y,p1.y - p2.y);
            float fontSize = Math.max(widthDiff, heightDiff) /  (line.toString().length()) - 0.1f;
            float diff = Math.max(widthDiff, heightDiff);

            Text text = new Text(line.toString());
            Paragraph paragraph = new Paragraph();
            paragraph.add(text);
            paragraph.setFont(baseFont)
                    .setFontColor(Color.BLACK, 0f)
                    .setFontSize(fontSize)
                    .setFixedLeading(fontSize);

            var deltaX=widthDiff*0.15f;
            paragraph.setFixedPosition(pageNum, p0.x+deltaX, p0.y, diff);
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
