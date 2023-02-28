package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.PDFUtils;
import com.example.sbimgutil.utils.TifUtils;
import fun.gatsby.commons.schedule.ITask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VolumeDirProcessTask implements ITask {

    static {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg2000");
        ImageWriter writer = writers.next();
    }

    public static final Set<String> SUPORTTED_FORMATS = Set.of("pdf", "jp2", "jpg","tif","tiff");

    public static final FileFilter supported_file_filter = file -> {
        if (file.isDirectory())
            return true;
        String lowerCasedName = file.getName().toLowerCase();
        return SUPORTTED_FORMATS.stream().anyMatch(lowerCasedName::endsWith);
    };

    private final File volumeDir;
    private final List<AppConfig.ProcessItem> processitemlist;

    public VolumeDirProcessTask(File volumeDir, List<AppConfig.ProcessItem> processitemlist) {
        this.volumeDir = volumeDir;
        this.processitemlist = processitemlist;
    }

    @Override
    public void before() {
        log.debug("开始处理书籍卷{}.",volumeDir);
    }

    @Override
    public void after() {

        CheckPoint checkPoint = ProcessExcutor.checkPoint;
        //处理pdf合并任务
        List<AppConfig.ProcessItem> pdfprocessItems = processitemlist.stream().filter(
                e -> "pdf".equals(e.getFormat())
        ).collect(Collectors.toList());

        for (AppConfig.ProcessItem processItem : pdfprocessItems) {
            try {
                if (!processItem.isEnable()) continue;
                log.debug("处理pdf整合流程,volume{}",volumeDir);
                processPdfItem(processItem);
                checkPoint.save(volumeDir, processItem.hashCode());
            } catch (Exception e) {
                log.error("{}目录书籍合并pdf出错", volumeDir, e);
                throw new RuntimeException(volumeDir.getAbsolutePath()+"卷处理出错",e);
            }
        }
    }

    @Override
    public void doWork() {

        final CheckPoint checkPoint = ProcessExcutor.checkPoint;
        final ConsoleProgressBar cpb = ProcessExcutor.consoleProgressBar;
        //处理pdf合并任务
        List<AppConfig.ProcessItem> nonPdfprocessItems = processitemlist.stream().filter(
                e -> !"pdf".equals(e.getFormat())
        ).collect(Collectors.toList());

        List<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, volumeDir
                ,supported_file_filter
        );
        files.sort(Comparator.comparing(File::getName));
        for (AppConfig.ProcessItem processItem : nonPdfprocessItems) {
            int i=0;
            if (!processItem.isEnable()) {
                continue;
            }
            for (File oriTifFile : files) {
                if (processItem.getFileNameRegex() != null) {
                    if (!oriTifFile.getName().matches(processItem.getFileNameRegex()))
                        continue;
                }
                String format = processItem.getFormat();
                try {
                    BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                    processOneItem(processItem, oriTifFile, format, bufferedImage);
                } catch (IOException e) {
                    log.error("{}文件读取错误，跳过该本书籍的该卷", oriTifFile, e);
                    // TODO: 2/22/2023 出错情况下的进度条处理
                    cpb.iterate();
                    throw new RuntimeException(volumeDir.getAbsolutePath()+"卷处理出错",e);
                }
                i++;
                if(i%nonPdfprocessItems.size()==0){
                    cpb.iterate();
                }
            }
            checkPoint.save(volumeDir, processItem.hashCode());
        }
    }

    void processOneItem(AppConfig.ProcessItem processItem, File oriTifFile, String format, BufferedImage bufferedImage) throws IOException {
        String outDirPath = processItem.getOutDirPath();
        int compressLimit = processItem.getCompressLimit();
        File outFile = genOutFile(oriTifFile, outDirPath, format);
        BufferedImage bufferedImageToSave = bufferedImage;
        if (processItem.isWithBlur()) {
            BufferedImage blurBufferedImage = ImageIO.read(new File(processItem.getBlurImagePath()));
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
                    long s = System.currentTimeMillis();
                    TifUtils.transformImgToJp2(bufferedImageToSave, Files.newOutputStream(outFile.toPath()));
                    log.debug("转换jp2无损耗时{}s,文件名{}", (System.currentTimeMillis() - s)/1000f, oriTifFile.getAbsolutePath());
                    return;
                }
                int compressTime = 1;
                long oriFileSizeM = oriTifFile.length() / (1024 * 1024);
                while (fsize > limitM || fsize < limitM * 0.8) {
                    long s = System.currentTimeMillis();
                    TifUtils.transformImgToJp2(bufferedImageToSave, Files.newOutputStream(outFile.toPath()), 0.5f, encoding);
                    fsize = outFile.length() / (1024 * 1024f);
                    log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},耗时{}s,文件名{}", compressTime,
                            fsize,oriFileSizeM , encoding,
                            (System.currentTimeMillis() - s)/1000f,
                            oriTifFile.getAbsolutePath()
                    );
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

    public void processPdfItem(AppConfig.ProcessItem processItem) throws Exception {

        File cataFile = new File(processItem.getCataDirPath(),volumeDir.getParentFile().getName()+"/"+volumeDir.getName()+".txt");

        String outDirPath = processItem.getOutDirPath();
        File pdfOutFile = new File(outDirPath, volumeDir.getParentFile().getName() + "/" + volumeDir.getName() + ".pdf");
        if (!pdfOutFile.getParentFile().exists())
            FileUtils.forceMkdirParent(pdfOutFile);

        File imgDir= new File(processItem.getResourceDirPath(), volumeDir.getParentFile().getName()+"/"+volumeDir.getName());

        FileFilter fileFilter = processItem.getFileNameRegex()!=null ?
             e-> e.getName().matches(processItem.getFileNameRegex()) : e->true;

        var fileArr = imgDir.listFiles(fileFilter);
        if(fileArr==null){
            log.error("卷{}下没有文件",imgDir.getAbsolutePath());
            return;
        }
        List<File> imgFiles = Arrays.asList(fileArr);
        imgFiles.sort(Comparator.comparing(File::getName));
        PDFUtils.mergeIntoPdf(imgFiles,cataFile, Files.newOutputStream(pdfOutFile.toPath()));
        FileUtils.copyFileToDirectory(cataFile,pdfOutFile.getParentFile());
    }

    File genOutFile(File oriFile, String outDirPath, String format) throws IOException {
        String fileAbsolutePath = oriFile.getAbsolutePath();
        String newFileAbsPath = fileAbsolutePath.replace(volumeDir.getAbsolutePath(), outDirPath);
        int pointIndex = newFileAbsPath.lastIndexOf(".");
        newFileAbsPath = newFileAbsPath.substring(0, pointIndex + 1) + format;
        File outFile = new File(newFileAbsPath);
        if (!outFile.getParentFile().exists())
            FileUtils.forceMkdir(outFile.getParentFile());
        return outFile;
    }
}

