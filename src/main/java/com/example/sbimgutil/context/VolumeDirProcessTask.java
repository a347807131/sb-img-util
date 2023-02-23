package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.PDFUtils;
import com.example.sbimgutil.utils.TifUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VolumeDirProcessTask implements ITask {

    public static final Set<String> SUPORTTED_FORMATS = Set.of("pdf", "jp2", "jpg");

    private final File volumeDir;
    private final List<AppConfig.ProcessConfigItem> processConfigItemList;

    public VolumeDirProcessTask(File volumeDir, List<AppConfig.ProcessConfigItem> processConfigItemList) {
        this.volumeDir = volumeDir;
        this.processConfigItemList = processConfigItemList;
    }

    @Override
    public void before() {
        log.debug("开始处理书籍卷{}.",volumeDir);
    }

    @Override
    public void after() {

        CheckPoint checkPoint = ProcessExcutor.checkPoint;
        //处理pdf合并任务
        List<AppConfig.ProcessConfigItem> pdfConfigItems = processConfigItemList.stream().filter(
                e -> "pdf".equals(e.getFormat()) && !checkPoint.checkIfFinished(volumeDir, e)
        ).collect(Collectors.toList());

        for (AppConfig.ProcessConfigItem configItem : pdfConfigItems) {
            try {
                if (!configItem.isEnable()) continue;
                log.debug("处理pdf整合流程,volume{}",volumeDir);
                processPdfItem(configItem);
            } catch (Exception e) {
                log.error("{}目录书籍合并pdf出错", volumeDir, e);
                throw new RuntimeException(volumeDir.getAbsolutePath()+"卷处理出错",e);
            }
            checkPoint.saveCheckPoint(volumeDir, configItem);
        }
    }

    @Override
    public void doWork() {

        final CheckPoint checkPoint = ProcessExcutor.checkPoint;
        final ConsoleProgressBar cpb = ProcessExcutor.consoleProgressBar;
        //处理pdf合并任务
        List<AppConfig.ProcessConfigItem> nonPdfConfigItems = processConfigItemList.stream().filter(
                e -> !"pdf".equals(e.getFormat()) && !checkPoint.checkIfFinished(volumeDir, e)
        ).collect(Collectors.toList());

        List<File> files = new LinkedList<>();
        // TODO: 2/23/2023 后续对于非tif文件也可处理
        FileFetchUtils.fetchFileRecursively(files, volumeDir,e->e.getName().endsWith("tif")||e.getName().endsWith("tiff"));
        files.sort(Comparator.comparing(File::getName));

        for (AppConfig.ProcessConfigItem configItem : nonPdfConfigItems) {
            int i=0;
            for (File oriTifFile : files) {
                if (!configItem.isEnable()) {
                    continue;
                }
                if (configItem.getFileNameReg() != null && !oriTifFile.getName().matches(configItem.getFileNameReg())) {
                    continue;
                }
                String format = configItem.getFormat();
                try {
                    BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                    processOneItem(configItem, oriTifFile, format, bufferedImage);
                } catch (IOException e) {
                    log.error("{}文件读取错误，跳过该本书籍的该卷", oriTifFile, e);
                    // TODO: 2/22/2023 出错情况下的进度条处理
                    cpb.iterate();
                    throw new RuntimeException(volumeDir.getAbsolutePath()+"卷处理出错",e);
                }
                i++;
                if(i%nonPdfConfigItems.size()==0){
                    cpb.iterate();
                }
            }
            checkPoint.saveCheckPoint(volumeDir, configItem);
        }
    }

    void processOneItem(AppConfig.ProcessConfigItem configItem, File oriTifFile, String format, BufferedImage bufferedImage) throws IOException {
        String outDirPath = configItem.getOutDirPath();
        int compressLimit = configItem.getCompressLimit();
        File outFile = genOutFile(oriTifFile, outDirPath, format);
        BufferedImage bufferedImageToSave = bufferedImage;
        if (configItem.isWithBlur()) {
            BufferedImage blurBufferedImage = ImageIO.read(new File(configItem.getBlurImagePath()));
            bufferedImageToSave = ImageIO.read(oriTifFile);
            float scale = bufferedImageToSave.getHeight() / (4f * blurBufferedImage.getHeight());
            TifUtils.drawBlurPic(bufferedImageToSave, blurBufferedImage, scale);
        }
        switch (format) {
            case "jp2": {
                float fsize = oriTifFile.length() / (1024f * 1024);
                float encoding = (float) (5.842e-6 * Math.pow(fsize, 2) - 0.002235 * fsize + 0.2732);
//                float encoding = -0.001f * fsize + 0.227f;
                float limitM = compressLimit / 1024f;
                if (limitM == 0) {
                    TifUtils.transformImgToJp2(bufferedImageToSave, Files.newOutputStream(outFile.toPath()));
                    return;
                }
                int compressTime = 1;
                long oriFileSizeM = oriTifFile.length() / (1024 * 1024);
                while (fsize > limitM || fsize < limitM * 0.8) {
                    TifUtils.transformImgToJp2(bufferedImageToSave, Files.newOutputStream(outFile.toPath()), 0.5f, encoding);
                    fsize = outFile.length() / (1024 * 1024f);
                    log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},文件名{}", compressTime,
                            fsize,oriFileSizeM , encoding, oriTifFile.getAbsolutePath());
                    compressTime += 1;
                    if (fsize > limitM)
                        encoding *=0.9;
                    else if (fsize < limitM * 0.8)
                        encoding *= 1.1;
                    else break;
                }
                break;
            }
            case "jpg": {
                long s = System.currentTimeMillis();
                TifUtils.transformImgToJpg(bufferedImageToSave, Files.newOutputStream(outFile.toPath()), compressLimit);
                log.debug("{}转化为jpg并输出共耗时{}s",oriTifFile,(System.currentTimeMillis()-s)/1000f);
                break;
            }
            default: {
                break;
            }
        }
    }

    public void processPdfItem(AppConfig.ProcessConfigItem configItem) throws Exception {

        File cataFile = new File(configItem.getCataDirPath(),volumeDir.getParentFile().getName()+"/"+volumeDir.getName()+".txt");

        String outDirPath = configItem.getOutDirPath();
        File pdfOutFile = new File(outDirPath, volumeDir.getParentFile().getName()+"/"+volumeDir.getName() + "/" + volumeDir.getName() + ".pdf");
        if (!pdfOutFile.getParentFile().exists())
            FileUtils.forceMkdirParent(pdfOutFile);

        File imgDir= new File(configItem.getResourceDirPath(), volumeDir.getParentFile().getName()+"/"+volumeDir.getName());

        LinkedList<File> imgFiles = new LinkedList<>();
        //可能需要过滤 todo
        FileFetchUtils.fetchFileRecursively(imgFiles, imgDir, (file) -> file.getName().endsWith(".jpg") || file.getName().endsWith(".jp2"));
        imgFiles.sort(Comparator.comparing(File::getName));
        PDFUtils.mergeIntoPdf(imgFiles,cataFile, Files.newOutputStream(pdfOutFile.toPath()));
    }

    File genOutFile(File oriTifFile, String outDirPath, String format) throws IOException {
        File tifDir = volumeDir.getParentFile().getParentFile();
        String fileAbsolutePath = oriTifFile.getAbsolutePath();
        String newFileAbsPath = fileAbsolutePath.replace(tifDir.getAbsolutePath(), outDirPath);

        int pointIndex = newFileAbsPath.lastIndexOf(".");
        newFileAbsPath = newFileAbsPath.substring(0, pointIndex + 1) + format;
        File outFile = new File(newFileAbsPath);
        if (!outFile.getParentFile().exists())
            FileUtils.forceMkdir(outFile.getParentFile());
        return outFile;
    }
}

