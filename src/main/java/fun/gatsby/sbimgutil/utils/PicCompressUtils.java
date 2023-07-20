package fun.gatsby.sbimgutil.utils;


import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.*;


/**
 * @author Gavin.luo
 * @title: PicUtils
 * @projectName MyData
 * @description:
 * @date 2021/7/20 17:30
 */
@Slf4j
public class PicCompressUtils {
    private static final Integer ZERO = 0;


    /**
     * fixme 压缩后的结果不对
     * 根据指定大小压缩图片
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize) {
        long s = System.currentTimeMillis();
        if(desFileSize<=0) return imageBytes;
        if (imageBytes == null || imageBytes.length <= ZERO || imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;

        try {
            while (imageBytes.length > desFileSize * 1024) {
                double accuracy = getAccuracy(imageBytes.length ,desFileSize*1024);
//                System.out.println(accuracy);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            log.debug("图片原大小={}kb | 压缩后大小={}kb | 耗时:{}s",
                    srcSize / 1024, imageBytes.length / 1024,
                    (System.currentTimeMillis()-s)/1000f
            );
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long srcSize,long destSize) {
        float ratio = srcSize / (float) destSize;
        double accuracy=1;
        for (int i = 0; i <5; i++) {
            accuracy-=0.1;
            int target = 1 << i;
            if(ratio <target) return accuracy;
        }
        return accuracy;
    }
}