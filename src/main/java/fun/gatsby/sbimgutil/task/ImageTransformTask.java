package fun.gatsby.sbimgutil.task;

import com.github.jaiimageio.impl.plugins.tiff.TIFFIFD;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadata;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class ImageTransformTask extends BaseTask{

    private final File inFile;
    private final String format;


    public ImageTransformTask(File inFile, File outFile, String format) {
        this.inFile = inFile;
        this.format = format;
        this.outFile = outFile;
        taskName = "格式转换: " + inFile.getName() + " to " + outFile.getAbsolutePath();
    }

    int oriWDpi, oriHDpi;

    @Override
    public void doWork() throws Exception {
        ImageInfo imageInfo = Imaging.getImageInfo(inFile);
        oriWDpi = imageInfo.getPhysicalWidthDpi();
        oriHDpi = imageInfo.getPhysicalHeightDpi();
        switch (format) {
            case "jp2" -> {
                BufferedImage bf = ImageIO.read(inFile);
                ImageIO.write(bf, "jpeg2000", outFile);
            }
            case "jpg", "jpeg" -> {
                this.transformToJpg();
            }
            case "tif", "tiff" -> {
                this.transformToTif();
            }
        }
    }

    /**
     * 保留dpi信息
     *
     * @throws Exception
     */
    private void transformToTif() throws Exception {

        BufferedImage bf = ImageIO.read(inFile);
        String name = null;
        ImageWriter writer = null;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        while (!Objects.equals(name, "com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter")) {
            writer = writers.next();
            name = writer.getClass().getName();
        }

        ImageWriteParam param = writer.getDefaultWriteParam();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);) {
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bf), param);
            // 获取图像元数据
            var tiffImageMetadata = (TIFFImageMetadata) metadata;
            TIFFIFD rootIFD = tiffImageMetadata.getRootIFD();
            TIFFField xResolutionField = tiffImageMetadata.getTIFFField(0x011a);
            TIFFField yResolutionField = tiffImageMetadata.getTIFFField(0x011b);
            var xdata = (long[][]) xResolutionField.getData();
            var ydata = (long[][]) yResolutionField.getData();
            xdata[0][0] = oriHDpi;
            ydata[0][0] = oriWDpi;
            rootIFD.addTIFFField(xResolutionField);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(bf, null, metadata), param);
            writer.dispose();
        }
    }

    private void transformToJpg() throws IOException {
        //##########################
        BufferedImage bf = ImageIO.read(inFile);
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);) {
            writer.setOutput(ios);
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bf), param);
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
            IIOMetadataNode jfif = (IIOMetadataNode) root.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("resUnits", "1");
            jfif.setAttribute("Xdensity", String.valueOf(oriWDpi));
            jfif.setAttribute("Ydensity", String.valueOf(oriHDpi));
            metadata.mergeTree(metadata.getNativeMetadataFormatName(), root);
            writer.write(null, new IIOImage(bf, null, metadata), param);
            writer.dispose();
        }
    }
}
