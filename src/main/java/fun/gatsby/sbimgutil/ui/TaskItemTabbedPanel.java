package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.ImageTransformTask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;
import fun.gatsby.sbimgutil.utils.Const;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TaskItemTabbedPanel extends JTabbedPane {


    private final Map<String, AppConfig.ProcessTask> taskMap;

    TaskItemTabbedPanel(Map<String, AppConfig.ProcessTask> taskMap){
        this.taskMap=taskMap;
        GuiUtils.setPreferredHeight(this,50);
        JBInit();
    }

    private void JBInit() {

        for (TaskTypeEnum typeEnum : Const.ENABLED_TASK_TYPES) {
            AppConfig.ProcessTask processTask = taskMap.getOrDefault(typeEnum.name(),new AppConfig.ProcessTask());
            switch (typeEnum){
                case IMAGE_TRANSFORM -> add(typeEnum.taskCnName, new ItemPanel() {
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
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                        String format = formatComboBox.getSelectedItem().toString();
                        processTask.setFormat(format);
                        return Map.entry(typeEnum,processTask);
                    }
                });
                case PDF_MERGE -> {
                    add(typeEnum.taskCnName, new ItemPanel() {
                        final FilePathInputPanel cataDirInputPanel;
                        {
                            cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
                            cataDirInputPanel.setFilePath(processTask.getCataDirPath());
                            add(cataDirInputPanel);
                        }
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                            processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                            return Map.entry(typeEnum,processTask);
                        }
                    });
                }
                case DRAW_BLUR -> {
                    add(typeEnum.taskCnName,new ItemPanel (){
                        final FilePathInputPanel blurImgFileInputPanel;
                        {
                            blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 10, JFileChooser.FILES_ONLY);
                            blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());
                            add(blurImgFileInputPanel);
                        }
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                            processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
                            return Map.entry(typeEnum,processTask);
                        }
                    });
                }

                case IMAGE_CUT -> add(typeEnum.taskCnName,new ItemPanel() {
                    final FilePathInputPanel labelFileInputPanel;
                    {
                        labelFileInputPanel = new FilePathInputPanel("裁切标注文件位置", 10, JFileChooser.FILES_ONLY);
                        labelFileInputPanel.setFilePath(processTask.getLabelFilePath());
                        add(labelFileInputPanel);
                    }

                    @Override
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                        processTask.setLabelFilePath(labelFileInputPanel.getFilePath());
                        return Map.entry(typeEnum,processTask);
                    }
                });
                case DOUBLE_LAYER_PDF_GENERATE -> add(typeEnum.taskCnName, new ItemPanel() {
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
                    public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                        processTask.setLabelDirPath(labelDirInputPanel.getFilePath());
                        return Map.entry(typeEnum,processTask);
                    }
                });
                case IMAGE_COMPRESS, FIVE_BACKSPACE_REPLACE-> add(typeEnum.taskCnName, new ItemPanel() {
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry() {
                            return Map.entry(typeEnum, processTask);
                        }
                    });
            }
        }
    }

    public abstract static class ItemPanel extends JPanel {
        public abstract Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidProcessTaskEntry();
    }
}
