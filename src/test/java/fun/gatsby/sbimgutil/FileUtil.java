package fun.gatsby.sbimgutil;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class FileUtil {

    /**
     * 1、验证base64是否为tiff或者tif格式
     * 文件名后缀为tif或tiff不代表图片就是对应的格式，需要验证base64
     *
     * @param base64
     * @return
     */
    public boolean checkImageBase64Format(String base64) {
        byte[] b = java.util.Base64.getDecoder().decode(base64);
        try {
            // 	判断是否为tiff格式
            if ((b[0] & 0xFF) == 0x49 && (b[1] & 0xFF) == 0x49 && (b[2] & 0xFF) == 0x2A) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 2、步骤1确定返回TRUE后，根据base64生成TIFF图片
     * 已经存在可视化tiff图片的请跳过这一步
     *
     * @param base64    tiff的base64
     * @param fileName  即将生成tiff图片的文件名
     * @param directory 生成tiff图片文件的目录
     */
    public void base64ToFile(String base64, String fileName, String directory) {
        File file = null;
        File dir = new File(directory);
        //	无目录的情况下创建一个目录,会受权限影响,最好是已存在的目录
        if (!dir.exists()) {
            dir.mkdirs();
        }
        java.io.FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            file = new File(directory + fileName);//目录+文件名作为输出文件的全路径
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    /**
     * 3、将图片文件tiff转化为图片文件jpg，生成新的文件
     *
     * @param oldPath 原图片的全路径（已存在的路径+已存在的文件名）
     * @param newPath 生成新的图片的存放目录（须已存在）
     */
    public void tiffToJpg(String oldPath, String newPath) {
        try {
            BufferedImage bufferegImage = ImageIO.read(new File(oldPath));
            ImageIO.write(bufferegImage, "jpg", new File(newPath));//可以是png等其它图片格式
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 补充一个将任意图片文件转为base64的方法，可协助步骤1的检验
     * 读字节是最快的方式
     *
     * @param filePath 图片文件的全路径
     * @return
     */
    public String imageToBase64(String filePath) {
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(filePath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return new String(Base64.encodeBase64(data));
    }

}




