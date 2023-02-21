package com.example.sbimgutil;

import com.example.sbimgutil.config.ProcessConfig;
import com.example.sbimgutil.context.CheckPoint;
import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.TifUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BookImageDirProcessTask implements ITask {

    static ConsoleProgressBar cpb =null;

    public static final Set<String> SUPORTTED_FORMATS=Set.of("pdf","jp2","jpg");

    private final File bookDir;
    private final List<ProcessConfig.ProcessConfigItem> processConfigItemList;

    public BookImageDirProcessTask(File bookDir,List<ProcessConfig.ProcessConfigItem> processConfigItemList){
        this.bookDir=bookDir;
        this.processConfigItemList=processConfigItemList;
    }

    @Override
    public void before() {
//        log.debug("开始处理{}下的书籍.",bookDir);
    }

    @Override
    public void after() {
//        log.debug("书籍{}处理完成",bookDir);
    }

    @Override
    public void doWork() {
        try {
            CheckPoint checkPoint=ProcessExcutor.checkPoint;

            File[] sectionDirs = bookDir.listFiles(checkPoint.getSectionDirFilter());
            if(sectionDirs==null) return;

            //处理pdf合并任务
            List<ProcessConfig.ProcessConfigItem> nonPdfConfigItems = processConfigItemList.stream().filter(
                    e -> !"pdf".equals(e.getFormat())).collect(Collectors.toList());
            //处理pdf合并任务
            List<ProcessConfig.ProcessConfigItem> pdfConfigItems = processConfigItemList.stream().filter(
                    e -> "pdf".equals(e.getFormat())).collect(Collectors.toList());

            sectionDirLoop:for (File sectionDir : sectionDirs) {
                log.info("开始处理卷{}下的书籍.",sectionDir);
                HashSet<File> files = new HashSet<>();
                FileFetchUtils.fetchFileRecursively(files,sectionDir);

                for (File oriTifFile : files) {
                    for (ProcessConfig.ProcessConfigItem configItem : nonPdfConfigItems) {
                        if(configItem.getFileNameReg()!=null && !oriTifFile.getName().matches(configItem.getFileNameReg()))
                            continue;
                        String format = configItem.getFormat();
                        if (!configItem.isEnable()) continue;
                        try {
                            BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                            processOneItem(configItem, oriTifFile, format, bufferedImage);
                        }catch (IOException  e){
                            log.error("{}文件读取错误，跳过该本书籍的该卷",oriTifFile,e);
                            cpb.iterate();
                           continue sectionDirLoop;
                        }
                    }
                    cpb.iterate();
                }
                // FIXME: 2/16/2023 还需要处理目录
                for (ProcessConfig.ProcessConfigItem pdfProcessConfigItem : pdfConfigItems) {
                    try {
                        if (!pdfProcessConfigItem.isEnable()) continue;
                        doMergeIntoPdf(pdfProcessConfigItem,sectionDir);
                    }catch (Exception e){
                        log.error("{}目录书籍合并pdf出错",bookDir,e);
                    }
                }

                String dataToSave=sectionDir.getAbsolutePath()+"\n";
                ProcessExcutor.checkPoint.saveCheckPoint(sectionDir);
            }
        }catch (Exception e){
            log.error("处理过程中出错",e);
            throw new RuntimeException(e);
        }
    }

    void processOneItem(ProcessConfig.ProcessConfigItem configItem,File oriTifFile,String format,BufferedImage bufferedImage) throws IOException {
        String outDirPath = configItem.getOutDirPath();
        int compressLimit = configItem.getCompressLimit();
        File outFile = genOutFile(oriTifFile, outDirPath, format);
        var bufferedImageToSave = bufferedImage;
        if (configItem.isWithBlur()) {
            BufferedImage blurBufferedImage = ImageIO.read(new File(configItem.getBlurImagePath()));
            bufferedImageToSave = ImageIO.read(oriTifFile);
            float scale= bufferedImageToSave.getHeight()/(4f* blurBufferedImage.getHeight());
            TifUtils.drawBlurPic(bufferedImageToSave, blurBufferedImage,scale);
        }
        switch (format) {
            case "jp2" :{
                float fsize = oriTifFile.length() / (1024f * 1024);

                float encoding = (float) (5.842e-06 * Math.pow(fsize, 2) - 0.002235 * fsize + 0.2732);
//                float i = 5.842e-06 * fsize ^ 2 - 0.002235 * fsize + 0.2732;
//                float encoding = -0.001f * fsize + 0.227f;
                float limitM = compressLimit / 1024f;
                if(limitM==0) {
                    TifUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile));
                    return;
                }
                int compressTime=1;
                while ( fsize > limitM || fsize < limitM * 0.8){
                    TifUtils.transformImgToJp2(bufferedImageToSave, new FileOutputStream(outFile),0.5f,encoding);
                    fsize = outFile.length() /(1024*1024f);
                    log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},文件名{}",compressTime,
                            fsize,oriTifFile.length()/1024,encoding,oriTifFile.getAbsolutePath());
                    compressTime+=1;
                    if(fsize>limitM)
                        encoding=-encoding/10+encoding;
                    else if(fsize<limitM*0.8)
                        encoding=encoding/10+encoding;
                    else break;
                }
                break;
            }
            case "jpg" : {
                TifUtils.transformImgToJpg(bufferedImageToSave, new FileOutputStream(outFile), compressLimit);
                break;
            }
            default : {
                break;
            }
        }
    }

    public void doMergeIntoPdf(ProcessConfig.ProcessConfigItem configItem, File sectionDir) throws IOException {

        log.debug("处理pdf整合流程");
        String outDirPath = configItem.getOutDirPath();

        File pdfOutFile = new File(outDirPath, bookDir.getName()+"/"+sectionDir.getName()+".pdf");
        if(!pdfOutFile.getParentFile().exists())
            FileUtils.forceMkdirParent(pdfOutFile);
        LinkedList<File> imgFiles = new LinkedList<>();
        //可能需要过滤
        FileFetchUtils.fetchFileRecursively(imgFiles, sectionDir);
        TifUtils.mergeImgToPdf(imgFiles,new FileOutputStream(pdfOutFile));
    }

    File genOutFile(File oriTifFile, String outDirPath, String format) throws IOException {
        File fitDir = bookDir.getParentFile();
        String fileAbsolutePath = oriTifFile.getAbsolutePath();
        String newFileAbsPath = fileAbsolutePath.replace(fitDir.getAbsolutePath(), outDirPath);

        int pointIndex = newFileAbsPath.lastIndexOf(".");
        newFileAbsPath=newFileAbsPath.substring(0,pointIndex+1)+format;
        File outFile = new File(newFileAbsPath);
        if(!outFile.getParentFile().exists())
            FileUtils.forceMkdir(outFile.getParentFile());
        return outFile;
    }
}

