package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.ui.FilePathInputPanel;
import fun.gatsby.sbimgutil.ui.TaskItemTabbedPanel;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;

import javax.swing.*;
import java.util.Map;

public enum TaskTypeEnum {
    IMAGE_TRANSFORM("图片格式转换"),
    PDF_MERGE("pdf合并"),
    IMAGE_COMPRESS("JP2图片压缩"),
    DRAW_BLUR("绘制水印"),
    IMAGE_CUT("图片裁剪"),
    BOOK_IMAGE_FIX("书籍图片修复"),
    FIVE_BACKSPACE_REPLACE("五个空格替换"),
    DOUBLE_LAYER_PDF_GENERATE("生成双层pdf"),
    LABELED_DATASET_COLLECT("ocr标记数据整理"),
    PDF_SPLIT("pdf拆分"),
    PDF_ADD_CATA("pdf添加目录"),
    PDF_IMAGE_SCALE("pdf图片缩放")
    ;
    public final String taskCnName;
    TaskTypeEnum(String taskCnName) {
        this.taskCnName = taskCnName;
    }

    public JPanel newTaskItemTabbedPanel(AppConfig.ProcessTask processTask) {
        switch (this){
            case PDF_IMAGE_SCALE -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    final JSpinner scaleSpinner;
                    {
                        scaleSpinner = new JSpinner(new SpinnerNumberModel(1, 0.1, 10, 0.1));
                        JLabel scaleLabel = new JLabel("缩放比例");
                        scaleSpinner.setValue(processTask.getPdfImageScale());
                        add(scaleLabel);
                        add(scaleSpinner);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setPdfImageScale(Float.parseFloat(scaleSpinner.getValue().toString()));
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case IMAGE_TRANSFORM -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    final JComboBox<String> formatComboBox;
                    {
                        formatComboBox = new JComboBox<>();
                        JLabel formatLabel = new JLabel("目标格式");
                        ImageTransformTask.SUPORTTED_TARGET_FORMAT.forEach(formatComboBox::addItem);
                        formatComboBox.setSelectedItem(processTask.getFormat());
                        add(formatLabel);
                        add(formatComboBox);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        String format = formatComboBox.getSelectedItem().toString();
                        processTask.setFormat(format);
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case PDF_MERGE -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    final FilePathInputPanel cataDirInputPanel;
                    final JSpinner scaleSpinner;
                    {
                        scaleSpinner = new JSpinner(new SpinnerNumberModel(1, 0.05, 1, 0.05));
                        JLabel scaleLabel = new JLabel("缩放比例");
                        scaleSpinner.setValue(processTask.getPdfImageScale());
                        JPanel scalePanel = GuiUtils.getHorizontalBoxLayoutPanel(   10, scaleLabel, scaleSpinner);
                        add(scalePanel);
                        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
                        cataDirInputPanel.setFilePath(processTask.getCataDirPath());
                        add(cataDirInputPanel);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                        processTask.setPdfImageScale(Float.parseFloat(scaleSpinner.getValue().toString()));
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case PDF_ADD_CATA -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    final FilePathInputPanel cataDirInputPanel;
                    {
                        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
                        cataDirInputPanel.setFilePath(processTask.getCataDirPath());
                        add(cataDirInputPanel);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case DRAW_BLUR -> {
                return new TaskItemTabbedPanel.ItemPanel(){
                    final FilePathInputPanel blurImgFileInputPanel;
                    {
                        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 10, JFileChooser.FILES_ONLY);
                        blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());
                        add(blurImgFileInputPanel);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case IMAGE_CUT -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    final FilePathInputPanel labelFileInputPanel;
                    {
                        labelFileInputPanel = new FilePathInputPanel("裁切标注文件位置", 10, JFileChooser.FILES_ONLY);
                        labelFileInputPanel.setFilePath(processTask.getLabelFilePath());
                        add(labelFileInputPanel);
                    }

                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setLabelFilePath(labelFileInputPanel.getFilePath());
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            case DOUBLE_LAYER_PDF_GENERATE -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    private final FilePathInputPanel labelDirInputPanel;
                    final FilePathInputPanel cataDirInputPanel;
                    {
                        this.cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
                        this.labelDirInputPanel = new FilePathInputPanel("标注文件所在文件夹", 10);
                        cataDirInputPanel.setFilePath(processTask.getCataDirPath());
                        labelDirInputPanel.setFilePath(processTask.getLabelDirPath());
                        add(cataDirInputPanel);
                        add(labelDirInputPanel);
                    }
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                        processTask.setLabelDirPath(labelDirInputPanel.getFilePath());
                        return Map.entry(TaskTypeEnum.this,processTask);
                    }
                };
            }
            default -> {
                return new TaskItemTabbedPanel.ItemPanel() {
                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry() {
                        return Map.entry(TaskTypeEnum.this, processTask);
                    }
                };
            }
        }
    }

    public BaseTaskGenerator newTaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
        BaseTaskGenerator taskGenerator=null;
        switch (this) {
            case PDF_MERGE -> taskGenerator = new PdfMergeTask.TaskGenerator(gtc, processTask);
            case DOUBLE_LAYER_PDF_GENERATE -> taskGenerator = new DoubleLayerPdfGenerateTask.TaskGenerator(gtc, processTask);
            case LABELED_DATASET_COLLECT -> taskGenerator=new LabeledDatasetCollectTask.TaskGenerator(gtc, processTask);
            case IMAGE_CUT -> taskGenerator = new ImageCutTask.TaskGenerator(gtc, processTask);
            case PDF_SPLIT -> taskGenerator = new PdfSplitTask.TaskGenerator(gtc, processTask);
            case PDF_ADD_CATA -> taskGenerator = new PdfAddCataTask.TaskGenerator(gtc, processTask);
            case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR,BOOK_IMAGE_FIX ,FIVE_BACKSPACE_REPLACE ->
                    taskGenerator = new BaseTaskGenerator(gtc, processTask,this);
        }
        return taskGenerator;
    }
}
