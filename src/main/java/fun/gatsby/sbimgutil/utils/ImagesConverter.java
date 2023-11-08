package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张治忠
 */
public class ImagesConverter {

    private static BaseFont baseFont = null;

    public ImagesConverter(String rootDir){
        this.rootDir=rootDir;
    }

    static {
        try {
            baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private String rootDir;


    /**
     * 图片列表，后面自行调用接口实现
     *
     * @return
     */
    public List<File> imageFiles() {
        return Arrays.stream(Objects.requireNonNull(new File(rootDir).listFiles())).toList()
                .stream()
                .filter(file -> file.getName().toLowerCase(Locale.ROOT).endsWith(".jpg"))
                .collect(Collectors.toList());
    }

    /**
     * 文件ocr识别，后面自行调用接口实现
     *
     * @param file 图片文件
     * @return
     */
    public List<OcrItemResult> imageOcr(File file) {
        List<String> lines = FileUtil.readUtf8Lines(new File(file.getParent(), "Label.txt"));
        Optional<String> targetLine = lines.stream().filter(line -> {
            String[] split = line.split("\t");
            if (split.length < 2) {
                return false;
            }
            return split[0].endsWith(file.getName());
        }).findFirst();
        if(targetLine.isEmpty())
            return Collections.emptyList();
        String[] split = targetLine.get().split("\t");
        if (split.length < 2) {
            return Collections.emptyList();
        }
        String json = split[1];
        return JSONUtil.toList(json, OcrItemResult.class);
    }

    /**
     * 得到图片的尺寸、缩放比例等信息
     * @param imageFile 图片文件
     * @return
     * @throws Exception
     */
    private RectInfo getRectInfo(File imageFile) throws Exception {
        byte[] imageData = FileUtil.readBytes(imageFile);
        Image image = Image.getInstance(imageData);
        float width = image.getWidth();
        float height = image.getHeight();
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
        image.setAlignment(Image.ALIGN_CENTER);
        return new RectInfo(width, height, newWidth, newHeight, newWidth / width, image);
    }

    /**
     * 转换成双层pdf
     * @param imageFiles 图片文件列表
     * @param pdfPath pdf路径
     * @throws Exception
     */
    public void convertToBilayerPdf(List<File> imageFiles, String pdfPath) throws Exception {
        if (CollUtil.isEmpty(imageFiles)) {
            return;
        }
        RectInfo firstRect = this.getRectInfo(imageFiles.get(0));
        Document doc = new Document(new Rectangle(firstRect.getNewWidth(), firstRect.getNewHeight()), 0, 0, 0, 0);
        FileOutputStream fileOutputStream = new FileOutputStream(pdfPath);
        PdfWriter writer = PdfWriter.getInstance(doc, fileOutputStream);
        doc.open();
        for (int i = 0; i < imageFiles.size(); i++) {
            File imageFile = imageFiles.get(i);
            RectInfo rectInfo = this.getRectInfo(imageFile);
            doc.setPageSize(new Rectangle(rectInfo.getNewWidth(), rectInfo.getNewHeight()));
            this.insertTextBoxes(imageFile, writer, rectInfo);
            doc.add(rectInfo.getImage());
            doc.newPage();

        }

        doc.close();
        writer.close();
        IoUtil.close(fileOutputStream);
    }

    /**
     * 插入透明文字
     * @param imageFile 图片文件
     * @param writer pdf writer
     * @param rectInfo 尺寸信息
     * @throws Exception
     */
    private void insertTextBoxes(File imageFile, PdfWriter writer, RectInfo rectInfo) throws Exception {
        List<OcrItemResult> ocrItemResults = this.imageOcr(imageFile);
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

            Font font = new Font(baseFont, fontSize, Font.NORMAL, new BaseColor(0, 0, 0, 0));
            Paragraph paragraph = new Paragraph();
            Phrase ph = new Phrase();
            ph.add(new Chunk(ocrItemResult.transcription, font));
            paragraph.add(ph);
            paragraph.setSpacingBefore(0);
            paragraph.setLeading(fontSize);
            Rectangle rect = new Rectangle(leftBottomPoint[0], height - leftBottomPoint[1], rightTopPoint[0], height - rightTopPoint[1]);//文本框位置

            writer.getDirectContent().rectangle(rect);
            ColumnText ct = new ColumnText(writer.getDirectContent());
            ct.addElement(paragraph);
            ct.setSimpleColumn(rect);
            ct.go();
        }
    }


    public static void main(String[] args) {
        try {
            ImagesConverter imagesConverter = new ImagesConverter("D:\\五经五卷-0007");
            imagesConverter.convertToBilayerPdf(imagesConverter.imageFiles(), "D:\\0007.pdf");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


}
