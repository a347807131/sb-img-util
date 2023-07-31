package fun.gatsby.sbimgutil.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 接口设计
 */
public interface ImageDPIProcessor {

    /**
     * 根据文件后缀扩展名判断是否能进行处理
     *
     * @param fileName
     * @return
     */
    boolean canHandle(String fileName);

    /**
     * @param path
     * @param dpi  dot per inch
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    void process(BufferedImage image, int dpi, OutputStream os) throws IOException;
}
