package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.builder.*;
import lombok.Getter;

@Getter
public enum TaskEnum {
    IMAGE_TRANSFORM("图片转换", ImageTransformTaskBuilder.class),
    PDF_MERGE("pdf合并", PdfMergeTaskBuilder.class),
    JP2_COMPRESS("JP2图片压缩", Jp2CompressTaskBuilder.class),
    DRAW_BLUR("绘制水印", DrawBlurTaskBuilder.class),
    PDF_SPLIT("pdf拆分", PdfMergeTaskBuilder.class),
    PDF_ADD_CATA("pdf添加目录", PdfAddCateTaskBuilder.class),
    PUNCTUATE("自然语言断句", PunctuateTaskBuilder.class),
    SOUNDTRACK_EXTRACT("音频提取", SoundtrackExtractTaskBuilder.class)
    ;
    public final String cnName;
    public final Class<? extends TaskBuilder<? extends ITask>> builderClass;
    TaskEnum(String taskCnName,Class<? extends TaskBuilder<? extends ITask>> builderClass) {
        this.cnName = taskCnName;
        this.builderClass =builderClass;
    }
}
