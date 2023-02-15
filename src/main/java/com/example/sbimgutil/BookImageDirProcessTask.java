package com.example.sbimgutil;

import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.TifUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class BookImageDirProcessTask implements ITask {

    static ConsoleProgressBar cpb =null;

    static final Set<String> SUPORTTED_FORMATS=Set.of("pdf","jp2","jpg");

    private final File bookDir;
    private final List<ProcessConfigItem> processConfigItemList;
    public static File checkPointFile;

    public BookImageDirProcessTask(File bookDir,List<ProcessConfigItem> processConfigItemList){
        this.bookDir=bookDir;
        this.processConfigItemList=processConfigItemList;
    }

    @Override
    public void before() {
        log.debug("开始处理{}下的书籍.",bookDir);
    }

    @Override
    public void after() {
        log.debug("{}处理完成",bookDir);
        try {
            FileUtils.writeStringToFile(checkPointFile,bookDir.getName()+"\n", Charset.defaultCharset(),true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doWork() {
        try {
            HashSet<File> files = new HashSet<>();
            FileFilter fileFilter = file -> file.isDirectory()||file.getName().endsWith(".tif");
            FileFetchUtils.fetchFileRecursively(files,bookDir,fileFilter);
            for (File oriTifFile : files) {
                for (ProcessConfigItem configItem : processConfigItemList) {
                    if(!configItem.isEnable() || configItem.getFormat().equals("pdf")) continue;
                    if(configItem.fileNameReg!=null && !oriTifFile.getName().matches(configItem.getFileNameReg()))
                        continue;
                    String format = configItem.getFormat();
                    if (!configItem.enable) continue;
                    BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                    processOneItem(configItem,oriTifFile,format,bufferedImage);
                }
                cpb.iterate();
            }
            //处理pdf合并任务
            List<ProcessConfigItem> pdfConfigItems = processConfigItemList.stream().filter(
                    e -> "pdf".equals(e.getFormat())).toList();
            //还需要处理目录
            for (ProcessConfigItem pdfProcessConfigItem : pdfConfigItems) {
                if(!pdfProcessConfigItem.isEnable()) continue;
                doMergeIntoPdf(pdfProcessConfigItem);
            }
        }catch (Exception e){
            log.error("处理过程中出错",e);
            throw new RuntimeException(e);
        }
    }

    void processOneItem(ProcessConfigItem configItem,File oriTifFile,String format,BufferedImage bufferedImage) throws IOException {
        switch (format) {
            case "jpg", "jp2" -> {
                String outDirPath = configItem.getOutDirPath();
                int compressLimit = configItem.getCompressLimit();
                File outFile = genOutFile(oriTifFile, outDirPath, format);
                var bufferedImageToSave = bufferedImage;
                if (configItem.isWithBlur()) {
                    BufferedImage blurBufferedImage = ImageIO.read(new File(configItem.getBlurImagePath()));
                    bufferedImageToSave = ImageIO.read(oriTifFile);
                    TifUtils.drawBlurPic(bufferedImageToSave, blurBufferedImage);
                }
                if(format.equals("jpg")) {
                    TifUtils.transformImgToJpg(bufferedImageToSave, new FileOutputStream(outFile), compressLimit);
                }else {
                    TifUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile), compressLimit);
                }
            }
            default -> {
            }
        }
    }

    public void doMergeIntoPdf(ProcessConfigItem configItem) throws IOException {

        log.debug("处理pdf整合流程");

        File bookDirT = new File(configItem.getResourceDirPath(), bookDir.getName());
        String outDirPath = configItem.getOutDirPath();
        File[] secionDirs = bookDirT.listFiles(File::isDirectory);

        if(secionDirs==null){
            log.warn("目标文件夹{}下没有东西,无法进行pdf合并操作，请检查配置",bookDirT);
            return;
        }

        for (File secionDir : secionDirs) {
            File pdfOutFile = new File(outDirPath, bookDir.getName()+"/"+secionDir.getName()+".pdf");
            if(!pdfOutFile.getParentFile().exists())
                FileUtils.forceMkdirParent(pdfOutFile);
            LinkedList<File> imgFiles = new LinkedList<>();
            //可能需要过滤
            FileFetchUtils.fetchFileRecursively(imgFiles, secionDir);
            TifUtils.mergeImgToPdf(imgFiles,new FileOutputStream(pdfOutFile));
        }
    }

    File genOutFile(File oriTifFile, String outDirPath, String format) throws IOException {
        File fitDir = bookDir.getParentFile();
        String fileAbsolutePath = oriTifFile.getAbsolutePath();
        String newFileAbsPath = fileAbsolutePath.replace(fitDir.getAbsolutePath(), outDirPath);

        newFileAbsPath = newFileAbsPath.substring(0, newFileAbsPath.length() - 3)+format;
        File outFile = new File(newFileAbsPath);
        if(!outFile.getParentFile().exists())
            FileUtils.forceMkdir(outFile.getParentFile());
        return outFile;
    }
}

