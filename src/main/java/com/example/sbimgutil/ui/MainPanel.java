package com.example.sbimgutil.ui;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.context.TaskExcutor;
import com.example.sbimgutil.task.TaskTypeEnum;
import com.example.sbimgutil.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class MainPanel extends JPanel {
    final
    AppConfig appConfig;

    CommonInputPanel workNumInputPanel;

    WorkItemChoosePanel taskItemChoosePanel;
    private JProgressBar progressBar;

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;

    CommonInputPanel fileNameRegInputPanel;

//    WorkItemPanel workItemPanel = new WorkItemPanel(null);
    private FilePathInputPanel cataDirInputPanel;
    private FilePathInputPanel blurImgFileInputPanel;


    public MainPanel(AppConfig appConfig) {
        super();
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
                try {
//                    URL resource = ClassLoader.getSystemClassLoader().getResource("程序说明.pdf");
//                    System.out.println(resource.getPath());
                    Desktop.getDesktop().open(new File("app/程序说明.pdf"));
                } catch (IOException ex) {
                    log.error("打开说明文件失败", ex);
                    throw new RuntimeException(ex);
                }
            }
        });

        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(appConfig.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", "^(?!seka).*$", 10);
        JPanel nameRegAndWokerNumWrapperPanel = new JPanel();
        nameRegAndWokerNumWrapperPanel.setLayout(new BoxLayout(nameRegAndWokerNumWrapperPanel, BoxLayout.X_AXIS));
        nameRegAndWokerNumWrapperPanel.add(fileNameRegInputPanel);
        nameRegAndWokerNumWrapperPanel.add(workNumInputPanel);
        add(nameRegAndWokerNumWrapperPanel);


        JPanel pathWrapperPanel = new JPanel();
        pathWrapperPanel.setLayout(new BoxLayout(pathWrapperPanel, BoxLayout.X_AXIS));

        pathInputPanel = new FilePathInputPanel("输入文件夹", 10);
//        pathInputPanel.setFilePath(processTask.getInDirPath());

        pathOutPanel = new FilePathInputPanel("输出文件夹", 10);
//        pathOutPanel.setFilePath(processTask.getOutDirPath());
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
        formatComboBox.addItem("");
        for (String format : Const.SUPORTTED_FORMATS) {
            formatComboBox.addItem(format);
        }

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
            processTask.setOutDirPath(pathOutPanel.getFilePath());

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
                JOptionPane.showMessageDialog(this, "任务已经开始");
            } catch (Exception ex) {
                JDialog dialog = new JDialog();
                dialog.setTitle("错误");
                dialog.add(new JLabel(ex.getMessage()));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    void addWorkTab() {


    }
}
