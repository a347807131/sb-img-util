package com.example.sbimgutil.context;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.ITask;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import com.example.sbimgutil.utils.PDFUtils;
import com.example.sbimgutil.utils.TifUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    /**
     * 相对于out文件夹的输出文件所在的文件夹的路径
     */
    private final String relativePath;

    public VolumeDirProcessTask(File volumeDir, String relativePath, List<AppConfig.ProcessItem> processitemlist) {
        this.volumeDir = volumeDir;
        this.relativePath = relativePath;
        this.processitemlist = processitemlist;
    }

    @Override
    public void before() {
        log.debug("开始处理书籍卷{}.",volumeDir);
    }

    @Override
    public void after() {

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
            for (File oriTifFile : files) {
                if (!processItem.isEnable()) {
                    continue;
                }
                if (processItem.getFileNameRegex() != null) {
                    if (!oriTifFile.getName().matches(processItem.getFileNameRegex()))
                        continue;
                }
                String format = processItem.getFormat();
                try {
                    BufferedImage bufferedImage = ImageIO.read(oriTifFile);
                    processOneItem(processItem, oriTifFile, format, bufferedImage);
                } catch (IOException e) {
                    log.error("{}文件处理错误，跳过该本书籍的该卷", oriTifFile, e);
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

    void processOneItem(AppConfig.ProcessItem processItem, File oriTifFile, String format, BufferedImage bufferedImage) throws IOException {
        LocalDateTime sTime = LocalDateTime.now();
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
        OutputStream os = Files.newOutputStream(outFile.toPath());
        switch (format) {
            case "jp2": {
                TifUtils.transformImgToJp2(bufferedImageToSave, outFile, compressLimit);
                break;
            }
            case "jpg": {
                TifUtils.transformImgToJpg(bufferedImageToSave, os, compressLimit);
                break;
            }
            default: {
                break;
            }
        }
        if(os!=null)
            os.close();
        long between = LocalDateTimeUtil.between(sTime, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("处理{}文件共计耗时 {} s,是否压缩:{}", oriTifFile, between, compressLimit > 0);
    }

    public void processPdfItem(AppConfig.ProcessItem processItem) throws Exception {

        //pdf的输出文件单独处理
        File cataFile = new File(processItem.getCataDirPath(),volumeDir.getParentFile().getName()+"/"+volumeDir.getName()+".txt");

        if(!cataFile.exists()){
            log.error("卷{}下没有目录文件",cataFile.getAbsolutePath());
            throw new IOException(cataFile.getAbsolutePath()+"下没有目录文件");
        }
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

    File genOutFile(File inFile, String outDirPath, String format) throws IOException {
        String inFileName = inFile.getName();
        File outFileDir = new File(outDirPath, relativePath);
        int pointIndex = inFileName.lastIndexOf(".");
        String outFileName = inFileName.substring(0, pointIndex + 1) + format;
        File outFile = new File(outFileDir,outFileName);
        if (!outFile.getParentFile().exists())
            FileUtils.forceMkdir(outFile.getParentFile());
        return outFile;
    }
}

