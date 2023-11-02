package fun.gatsby.sbimgutil.task;

public enum TaskTypeEnum {
    IMAGE_TRANSFORM("图片格式转换"),
    PDF_MERGE("pdf合并"),
    IMAGE_COMPRESS("JP2图片压缩"),
    DRAW_BLUR("绘制水印"),
    IMAGE_CUT("图片裁剪"),
    BOOK_IMAGE_FIX("书籍图片修复"),
    FIVE_BACKSPACE_REPLACE("五个空格替换"),
    SEARCH_ABLE_PDF_GENERATE("生成双层pdf"),
    ;
    public final String taskCnName;

    TaskTypeEnum(String taskCnName) {
        this.taskCnName = taskCnName;
    }

    public static TaskTypeEnum parse(String name) {
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.name().equals(name)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}
