package com.example.sbimgutil;

import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.Const;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Slf4j
public class BookImageDirProcessTask implements ITask {

    static ConsoleProgressBar cpb =null;

    static final Set<String> SUPORTTED_FORMATS=Set.of("pdf","jp2","jpg");

    private final File bookDir;
    private final List<ProcessConfigItem> processConfigItemList;

    public BookImageDirProcessTask(File bookDir,List<ProcessConfigItem> processConfigItemList){
        this.bookDir=bookDir;
        this.processConfigItemList=processConfigItemList;
    }

    @Override
    public void before() {
        log.info("开始处理{}下的书籍.",bookDir);
    }

    @Override
    public void after() {

    }

    @Override
    public void doWork() {
        try {
            List<File> files = new LinkedList<>();
            FileFilter fileFilter = file -> file.isDirectory()||file.getName().endsWith(".tif");
            FileFetchUtils.fetchFileRecursively(files,bookDir,fileFilter);
            log.info("{}下共有{}张图片待处理",bookDir,files.size());
            for (File oriTifFile : files) {
                long s = System.currentTimeMillis();

                File outFile;
                for (ProcessConfigItem configItem : processConfigItemList) {
                    String format = configItem.getFormat();
                    if (!configItem.enable) continue;
                    BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                    if ("jp2".equals(format)) {
                        String outDirPath = configItem.getOutDirPath();
                        int compressLimit = configItem.getCompressLimit();
                        outFile = genOutFile(oriTifFile, outDirPath, format);

                        var bufferedImageToSave = bufferedImage;
                        if (configItem.isWithBlur()) {
                            BufferedImage blurBufferedImage = ImageIO.read(new File(configItem.getBlurImagePath()));
                            bufferedImageToSave = ImageIO.read(oriTifFile);
                            TifUtils.drawBlurPic(bufferedImageToSave, blurBufferedImage);
                        }
                        TifUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), compressLimit);
//
                    } else if ("jpg".equals(format)) {
                        String outDirPath = configItem.getOutDirPath();
                        int compressLimit = configItem.getCompressLimit();
                        outFile = genOutFile(oriTifFile, outDirPath, format);

                        var bufferedImageToSave = bufferedImage;
                        if (configItem.isWithBlur()) {
                            bufferedImageToSave = ImageIO.read(oriTifFile);
                            TifUtils.drawBlurPic(bufferedImageToSave,
                                    ImageIO.read(new File(configItem.getBlurImagePath()))
                            );
                        }
                        TifUtils.transformImgToJpg(bufferedImageToSave, new FileOutputStream(outFile), compressLimit);
                    } else if ("pdf".equals(format)) {
                        // TODO: 2/10/2023
                        log.debug("处理pdf整合流程");
                    }
                }
                cpb.iterate();

            }
            log.info("{}处理完成",bookDir);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    File genOutFile(File oriTifFile, String outDirPath, String format) throws IOException {
        File fitDir = bookDir.getParentFile();
        String fileAbsolutePath = oriTifFile.getAbsolutePath();
        String newFileAbsPath = fileAbsolutePath.replace(fitDir.getAbsolutePath(), outDirPath);
        newFileAbsPath = newFileAbsPath.substring(0, newFileAbsPath.length() - 3)+format;
        File outFile = new File(newFileAbsPath);
        if(!outFile.getParentFile().exists())
            FileUtils.forceMkdirParent(outFile);
        return outFile;
    }
}

