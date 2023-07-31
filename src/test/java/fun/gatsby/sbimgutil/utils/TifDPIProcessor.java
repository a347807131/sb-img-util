package fun.gatsby.sbimgutil.utils;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class TifDPIProcessor implements ImageDPIProcessor {
    //1英寸是2.54里面
    private static final double INCH_2_CM = 2.54d;

    @Override
    public boolean canHandle(String fileName) {
        return true;
    }

    @Override
    public void process(BufferedImage image, int dpi, OutputStream os) throws IOException {
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("tif").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
        setDPI(metadata, dpi);
        writer.setOutput(ios);
        writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
    }

    private void setDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter
        double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;
        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

}