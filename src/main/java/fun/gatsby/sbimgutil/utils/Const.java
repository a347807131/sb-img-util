package fun.gatsby.sbimgutil.utils;

import fun.gatsby.sbimgutil.task.TaskTypeEnum;

import java.io.FileFilter;
import java.util.List;

import static fun.gatsby.sbimgutil.task.TaskTypeEnum.*;

public interface Const {

    List<String> SUPORTTED_FORMATS = List.of(
            "jpg", "jp2", "tif", "tiff","jpeg","txt"
    );

    FileFilter SUPPORTED_FILE_FILTER = file -> {
        if (file.isDirectory())
            return true;
        String lowerCasedName = file.getName().toLowerCase();
        return Const.SUPORTTED_FORMATS.stream().anyMatch(lowerCasedName::endsWith);
    };

    List<TaskTypeEnum> ENABLED_TASK_TYPES=List.of(
            IMAGE_TRANSFORM,
            PDF_MERGE,
            IMAGE_COMPRESS,
            DRAW_BLUR,
            IMAGE_CUT,
            FIVE_BACKSPACE_REPLACE,
            OCR_LABELED_DATASET_XML_GENERATE
    );
}
