package fun.gatsby.sbimgutil.utils;

import fun.gatsby.sbimgutil.task.NlpTask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;

import java.io.FileFilter;
import java.util.List;

import static fun.gatsby.sbimgutil.task.TaskTypeEnum.*;

public interface Const {

    List<String> SUPORTTED_FORMATS = List.of(
            "jpg", "jp2", "tif", "tiff","jpeg","txt","pdf"
    );

    List<TaskTypeEnum> ENABLED_TASK_TYPES=List.of(
            IMAGE_TRANSFORM,
            PDF_MERGE,
            IMAGE_COMPRESS,
            DRAW_BLUR,

            NLP,
//            IMAGE_CUT,
//            DOUBLE_LAYER_PDF_GENERATE,
//            LABELED_DATASET_COLLECT,
            PDF_SPLIT,
            PDF_ADD_CATA
    );
}
