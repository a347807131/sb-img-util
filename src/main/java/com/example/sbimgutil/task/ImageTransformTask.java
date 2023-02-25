package com.example.sbimgutil.task;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.FileFetchUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.example.sbimgutil.context.VolumeDirProcessTask.supported_file_filter;

public class ImageTransformTask extends BaseTask{


    public static final Set<String> SUPORTTED_FORMATS = Set.of("pdf", "jp2", "jpg","tif","tiff");


    private final AppConfig.ProcessTask processItem;

    public ImageTransformTask(AppConfig.ProcessTask processItem) {
        this.processItem = processItem;
    }

    @Override
    public void doWork() {
        List<File> files = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(files, new File(processItem.getInDirPath())
                ,supported_file_filter
        );
        files.sort(Comparator.comparing(File::getName));
        for (File oriFile : files) {
            String format = processItem.getFormat();
            if (processItem.getFileNameRegex() != null) {
                if (!oriFile.getName().matches(processItem.getFileNameRegex()))
                    continue;
//                BufferedImage bufferedImage = ImageIO.read(oriFile);
//                pricessOneFile(oriFile, bufferedImage);
            }
        }
    }

}
