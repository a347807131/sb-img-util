package fun.gatsby.sbimgutil.utils;

import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class JpegDPIProcessor implements ImageDPIProcessor {

    private String formatName = "jpeg";

    @Override
    public void process(BufferedImage bf, int dpi, OutputStream os) throws IOException {
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(os);) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();

            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bf), param);
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
            IIOMetadataNode jfif = (IIOMetadataNode) root.getElementsByTagName("app0JFIF").item(0);

            jfif.setAttribute("resUnits", "1");
            jfif.setAttribute("Xdensity", dpi + "");
            jfif.setAttribute("Ydensity", dpi + "");

            metadata.mergeTree(metadata.getNativeMetadataFormatName(), root);
            writer.write(null, new IIOImage(bf, null, metadata), param);
        }
    }

    @Override
    public boolean canHandle(String fileName) {
        assert fileName != null : "fileName should not be null";

        return fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("JPG")
                || fileName.endsWith("JPEG");
    }

}