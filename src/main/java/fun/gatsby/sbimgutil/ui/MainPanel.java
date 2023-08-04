package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExcutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

@Slf4j
public class MainPanel extends JPanel {
    final
    AppConfig appConfig;

    CommonInputPanel workNumInputPanel;

    WorkItemChoosePanel taskItemChoosePanel;
    private JProgressBar progressBar;

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;

    CommonInputPanel fileNameRegInputPanel;

    private FilePathInputPanel cataDirInputPanel;
    private FilePathInputPanel blurImgFileInputPanel;
    private JRadioButton recursiveChooseBtn;


    public MainPanel(AppConfig appConfig) {
        this.appConfig = appConfig;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        init();
    }

    private void init() {

        JLabel label = new JLabel("<HTML><U>使用说明</U></HTML>");
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String url = appConfig.getReadmeUrl();
                try {
                    Runtime.getRuntime().exec("cmd /c start " + url);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(appConfig.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", "^(?!seka).*$", 10);
        recursiveChooseBtn = new JRadioButton("递归处理输入文件夹", true);

        JPanel nameRegAndWokerNumWrapperPanel = new JPanel();
        nameRegAndWokerNumWrapperPanel.setLayout(new BoxLayout(nameRegAndWokerNumWrapperPanel, BoxLayout.X_AXIS));
        nameRegAndWokerNumWrapperPanel.add(fileNameRegInputPanel);
        nameRegAndWokerNumWrapperPanel.add(workNumInputPanel);
        nameRegAndWokerNumWrapperPanel.add(recursiveChooseBtn);
        add(nameRegAndWokerNumWrapperPanel);


        JPanel pathWrapperPanel = new JPanel();
        pathWrapperPanel.setLayout(new BoxLayout(pathWrapperPanel, BoxLayout.X_AXIS));

        pathInputPanel = new FilePathInputPanel("输入文件夹", 10);

        pathOutPanel = new FilePathInputPanel("输出文件夹", 10);
        pathWrapperPanel.add(pathInputPanel);
        pathWrapperPanel.add(pathOutPanel);
        add(pathWrapperPanel);

        taskItemChoosePanel = new WorkItemChoosePanel();
        add(taskItemChoosePanel);


        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
//        cataDirInputPanel.setFilePath(processTask.getCataDirPath());

        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 10, JFileChooser.FILES_ONLY);
//        blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());

        FilePathInputPanel labelFileInputPanel = new FilePathInputPanel("裁切标注文件位置", 10, JFileChooser.FILES_ONLY);


        JPanel formatChosePanel = new JPanel();
        JLabel formatLabel = new JLabel("目标格式");
        JComboBox<String> formatComboBox = new JComboBox<>();
        formatComboBox.addItem("jpg");
        formatComboBox.addItem("jp2");
        formatComboBox.addItem("tif");

        formatChosePanel.add(formatLabel);
        formatChosePanel.add(formatComboBox);

        add(cataDirInputPanel);
        add(blurImgFileInputPanel);
        add(formatChosePanel);
        add(labelFileInputPanel);

        formatChosePanel.setVisible(true);
        cataDirInputPanel.setVisible(false);
        blurImgFileInputPanel.setVisible(false);
        labelFileInputPanel.setVisible(false);


        JButton startBtn = new JButton("开始");
        add(startBtn);

        taskItemChoosePanel.addItemListener(e -> {
            formatChosePanel.setVisible(false);
            cataDirInputPanel.setVisible(false);
            blurImgFileInputPanel.setVisible(false);

            String actionCommand = e.getActionCommand();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.parse(actionCommand);
            JPanel tagetPanel = switch (taskTypeEnum) {
                case IMAGE_TRANSFORM -> formatChosePanel;
                case PDF_MERGE -> cataDirInputPanel;
                case IMAGE_COMPRESS, IMAGE_CUT -> null;
                case DRAW_BLUR -> blurImgFileInputPanel;
            };
            if (tagetPanel != null)
                tagetPanel.setVisible(true);
        });

        startBtn.addActionListener(e -> {

            AppConfig.ProcessTask processTask = new AppConfig.ProcessTask();
            processTask.setFileNameRegex(fileNameRegInputPanel.getValue());
            processTask.setInDirPath(pathInputPanel.getFilePath());
            String outDirPath = pathOutPanel.getFilePath();
            processTask.setOutDirPath(outDirPath);
            processTask.setRecursive(recursiveChooseBtn.isSelected());

            TaskTypeEnum taskType = taskItemChoosePanel.getSelectedTaskType();
            processTask.setTaskType(taskType.name());
            switch (taskType) {
                case DRAW_BLUR -> processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
                case PDF_MERGE -> processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                case IMAGE_TRANSFORM -> processTask.setFormat((String) formatComboBox.getSelectedItem());
            }
            int maxWorkerNum = Integer.parseInt(workNumInputPanel.getValue());
            try {
                TaskExcutor taskExcutor = new TaskExcutor(processTask, taskType.name(), maxWorkerNum);
                taskExcutor.start();
                JOptionPane.showMessageDialog(this, "任务完成");
            } catch (Exception ex) {
                JDialog dialog = new JDialog();
                dialog.setTitle("错误");
                dialog.add(new JLabel(ex.getMessage()));
                dialog.setVisible(true);
            }
        });
    }
}
