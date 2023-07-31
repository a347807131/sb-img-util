package fun.gatsby.sbimgutil;

import com.github.jaiimageio.impl.plugins.tiff.TIFFIFD;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadata;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import fun.gatsby.sbimgutil.task.ImageTransformTask;
import fun.gatsby.sbimgutil.utils.ImageDPIProcessor;
import fun.gatsby.sbimgutil.utils.JpegDPIProcessor;
import fun.gatsby.sbimgutil.utils.TifDPIProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.*;

@Slf4j
public class ImageTransTest {
    File tifFile = new File("D:\\原始备份\\图片处理模板\\1 扫描原图（TIFF格式）\\27030166\\0001", "0001.tif");

    //bugfixed
    static {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tif");
        ImageWriter writer = writers.next();
        Iterator<ImageWriter> writers1 = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer1 = writers1.next();
    }

    @Test
    public void t5() throws IOException {
        BufferedImage bf = ImageIO.read(new File("D:\\原始备份\\图片处理模板\\tif2jpg\\27030166\\0001", "0001.jpg"));

        ImageDPIProcessor processor = new TifDPIProcessor();
        OutputStream os = Files.newOutputStream(Path.of("temp.tif"));
        processor.process(bf, 200, os);
        os.close();
    }

    @Test
    public void t6() throws IOException {

        File tiffFile = tifFile;

        // 读取tiff文件
        ImageInputStream iis = ImageIO.createImageInputStream(tiffFile);
        ImageReader reader = ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);
        // 获取图像元数据
        IIOMetadata metadata = reader.getImageMetadata(0);
        var tiffImageMetadata = (TIFFImageMetadata) metadata;
        TIFFIFD rootIFD = tiffImageMetadata.getRootIFD();

        // TODO: 2023/7/31
        TIFFField xResolutionField = tiffImageMetadata.getTIFFField(0x011a);
        TIFFField yResolutionField = tiffImageMetadata.getTIFFField(0x011b);
        var xdata = (long[][]) xResolutionField.getData();
        var ydata = (long[][]) yResolutionField.getData();
        xdata[0][0] = 300L;
        ydata[0][0] = 300L;
        log.info(rootIFD.toString());
        rootIFD.addTIFFField(xResolutionField);
        // 将修改后的元数据写回到图片
        ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        ImageOutputStream ios = ImageIO.createImageOutputStream(new File("temp.tif"));

        writer.setOutput(ios);

        BufferedImage bf = ImageIO.read(tiffFile);
        writer.write(null, new IIOImage(bf, null, metadata), param);
    }


    @Test
    public void t9() throws IOException {
        File tiffFile = tifFile;
        BufferedImage bf = ImageIO.read(tiffFile);

        // 读取tiff文件
        ImageInputStream iis = ImageIO.createImageInputStream(tiffFile);
        ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
        reader.setInput(iis);
        // 获取图像元数据
        IIOMetadata metadata = reader.getImageMetadata(0);
        var tiffImageMetadata = (TIFFImageMetadata) metadata;

        TIFFField xResolutionField = tiffImageMetadata.getTIFFField(0x011a);
        TIFFField yResolutionField = tiffImageMetadata.getTIFFField(0x011b);

        int xr = xResolutionField.getAsInt(0);
        System.out.println(xr);
        int yr = yResolutionField.getAsInt(0);
        System.out.println();
    }

    @Test
    public void t10() throws Exception {

        ImageTransformTask imageTransformTask = new ImageTransformTask(new File("temp.jpg"), new File("temp.tif"), "tif");
        imageTransformTask.doWork();
    }
}
