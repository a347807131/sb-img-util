package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.ImageTransformTask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;

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
        for (Map.Entry<String, AppConfig.ProcessTask> entry : taskMap.entrySet()) {
            String key = entry.getKey();
            AppConfig.ProcessTask processTask = entry.getValue();
            TaskTypeEnum typeEnum = TaskTypeEnum.valueOf(key);
            switch (typeEnum){
                case IMAGE_TRANSFORM -> {
                    class IMAGE_TRANSFORM_CFG_PANEL extends ItemPanel {
                        final JComboBox<String> formatComboBox;
                        IMAGE_TRANSFORM_CFG_PANEL(){
                            formatComboBox = new JComboBox<>();
                            JLabel formatLabel = new JLabel("目标格式");
                            ImageTransformTask.SUPORTTED_TARGET_FORMAT.forEach(formatComboBox::addItem);
                            formatComboBox.setSelectedItem(processTask.getFormat());
                            add(formatLabel);
                            add(formatComboBox);
                        }

                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                            String format = formatComboBox.getSelectedItem().toString();
                            processTask.setFormat(format);
                            return Map.entry(typeEnum,processTask);
                        }
                    }
                    add(typeEnum.taskCnName,new IMAGE_TRANSFORM_CFG_PANEL());
                }
                case PDF_MERGE -> {
                    class PDF_MERGE_CFG_PANEL extends ItemPanel {
                        FilePathInputPanel cataDirInputPanel;
                        PDF_MERGE_CFG_PANEL(){
                            cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
                            cataDirInputPanel.setFilePath(processTask.getCataDirPath());
                            add(cataDirInputPanel);
                        }
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                            processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                            return Map.entry(typeEnum,processTask);
                        }
                    }
                     add(typeEnum.taskCnName, new PDF_MERGE_CFG_PANEL());
                }
                case DRAW_BLUR -> {
                    class DRAW_BLUR_CFG_PANEL extends ItemPanel {
                        FilePathInputPanel blurImgFileInputPanel;
                        DRAW_BLUR_CFG_PANEL(){
                            blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 10, JFileChooser.FILES_ONLY);
                            blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());
                            add(blurImgFileInputPanel);
                        }
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                            processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
                            return Map.entry(typeEnum,processTask);
                        }
                    }
                    add(typeEnum.taskCnName,new DRAW_BLUR_CFG_PANEL());
                }
                case IMAGE_CUT -> {

                    class IMAGE_CUT_CFG_PANEL extends ItemPanel {
                        FilePathInputPanel labelFileInputPanel;
                        IMAGE_CUT_CFG_PANEL(){
                            labelFileInputPanel = new FilePathInputPanel("裁切标注文件位置", 10, JFileChooser.FILES_ONLY);
                            labelFileInputPanel.setFilePath(processTask.getLabelFilePath());
                            add(labelFileInputPanel);
                        }
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                            processTask.setLabelFilePath(labelFileInputPanel.getFilePath());
                            return Map.entry(typeEnum,processTask);
                        }
                    }
                    add(typeEnum.taskCnName,new IMAGE_CUT_CFG_PANEL());
                }
                case IMAGE_COMPRESS, FIVE_BACKSPACE_REPLACE -> {
                    add(typeEnum.taskCnName, new ItemPanel() {
                        @Override
                        public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                            return Map.entry(typeEnum,processTask);
                        }
                    });
                }
            }
        }
        new ItemPanel(){

            @Override
            public Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry() {
                return null;
            }
        };
    }

    public abstract class ItemPanel extends JPanel {
        public abstract Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getValidEntry();
    }
}
