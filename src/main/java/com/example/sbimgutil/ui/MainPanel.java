package com.example.sbimgutil.ui;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.context.TaskExcutor;
import com.example.sbimgutil.task.TaskTypeEnum;
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

        taskItemChoosePanel = new WorkItemChoosePanel();
        add(taskItemChoosePanel);
        String taskTypeName = taskItemChoosePanel.getSelectedTaskType().name();

        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(appConfig.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", "", 10);
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

        JPanel middlePanel = new JPanel();
        add(middlePanel);
//

        JButton startBtn = new JButton("开始");
        add(startBtn);

        taskItemChoosePanel.addItemListener(e -> {
            String actionCommand = e.getActionCommand();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.parse(actionCommand);
            switch (taskTypeEnum) {
                case IMAGE_COMPRESS -> {
                    middlePanel.removeAll();
                    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
                    middlePanel.add(new ImageCompressPanel());
                }
            }

            middlePanel.revalidate();
            middlePanel.repaint();
        });

        startBtn.addActionListener(e -> {

            AppConfig.ProcessTask processTask = workItemPanel[0].getProcessTask();
            TaskTypeEnum taskType = taskItemChoosePanel.getSelectedTaskType();
            processTask.setTaskType(taskType.name());
            int maxWorkerNum = Integer.parseInt(workNumInputPanel.getValue());
            try {
                TaskExcutor taskExcutor = new TaskExcutor(processTask, taskType.name(), maxWorkerNum);
                taskExcutor.start();
                JOptionPane.showMessageDialog(this, "任务完成");
            } catch (Exception ex) {
                JDialog dialog = new JDialog();
                dialog.setTitle("错误");
                dialog.add(new JLabel(ex.getMessage()));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }
}
