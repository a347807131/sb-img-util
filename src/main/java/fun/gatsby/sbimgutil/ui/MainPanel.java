package fun.gatsby.sbimgutil.ui;

import cn.hutool.core.lang.func.VoidFunc1;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static fun.gatsby.sbimgutil.ui.util.Insertable.*;

@Slf4j
public class MainPanel extends JPanel {
    final
    AppConfig appConfig;

    CommonInputPanel workNumInputPanel;
    private JProgressBar progressBar=new JProgressBar();

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;

    CommonInputPanel fileNameRegInputPanel;

    private JRadioButton recursiveChooseBtn;
    private JRadioButton enforceChooseBtn;


    public MainPanel(AppConfig appConfig) {
        this.appConfig = appConfig;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JbInit();
    }

    void JbInit() {

        TaskItemTabbedPanel taskItemTabbedPanel = new TaskItemTabbedPanel(appConfig.getProcessTasks());
        add(taskItemTabbedPanel);

        JLabel label = new JLabel("<HTML><U>使用说明</U></HTML>");
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    URI uri = URI.create(appConfig.getReadmeUrl());
                    GuiUtils.openInDefaultBrowser(MainPanel.this, uri.toURL());
                } catch (IOException ex) {
                    log.error("打开使用说明失败", ex);
                    throw new RuntimeException(ex);
                }
            }
        });
        JLabel logLabel = new JLabel("<HTML><U>打开日志文件夹</U></HTML>");
        logLabel.setForeground(Color.BLUE);
        logLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(logLabel);
        logLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var url = appConfig.getLoggingFilePath();
                GuiUtils.openSystemExplorer(MainPanel.this, new File(url));
            }
        });

        AppConfig.GlobalTaskConfig gtc = appConfig.getGlobalTaskConfig();

        pathInputPanel = new FilePathInputPanel("输入文件夹", 10);
        pathInputPanel.setFilePath(gtc.getInDirPath());
        pathOutPanel = new FilePathInputPanel("输出文件夹", 10);
        pathOutPanel.setFilePath(gtc.getOutDirPath());
        add(
            GuiUtils.getFlowLayoutPanel(
                FlowLayout.TRAILING, BLOCK_SEPARATOR, ITEM_SEPARATOR_SMALL, pathInputPanel, pathOutPanel)
        );


        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(gtc.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", gtc.getFileNameRegex(), 10);
        recursiveChooseBtn = new JRadioButton("递归文件处理", gtc.isRecursive());
        enforceChooseBtn = new JRadioButton("强制覆盖处理", gtc.isEnforce());

        add(
            GuiUtils.getFlowLayoutPanel(
                FlowLayout.TRAILING, BLOCK_SEPARATOR, ITEM_SEPARATOR_SMALL,
                workNumInputPanel,
                fileNameRegInputPanel,
                recursiveChooseBtn,
                enforceChooseBtn)
        );

        JButton startBtn = new JButton("开始");
        add(startBtn, BorderLayout.CENTER);
        progressBar.setString("任务进度");
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(getWidth(), 20));
        add(progressBar);

        startBtn.addActionListener(e -> {
            if(progressBar.getValue()!=0 && progressBar.getValue()!=progressBar.getMaximum()){
                JOptionPane.showMessageDialog(this, "任务正在执行中，请等待任务执行完毕");
                return;
            }

            gtc.setFileNameRegex(fileNameRegInputPanel.getValue());
            gtc.setInDirPath(pathInputPanel.getFilePath());
            gtc.setOutDirPath(pathOutPanel.getFilePath());
            gtc.setRecursive(recursiveChooseBtn.isSelected());
            gtc.setEnforce(enforceChooseBtn.isSelected());

            Component component = taskItemTabbedPanel.getSelectedComponent();
            if(component instanceof TaskItemTabbedPanel.ItemPanel itemPanel){

                Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry = itemPanel.getCurrentProcessTaskEntry();
                try {
                    progressBar.setValue(0);
                    ConsoleProgressBar cpb= new ConsoleProgressBar();
                    IntConsumer setTaskCountBeforeExcutionComsumer = (int taskCount) -> {
                        progressBar.setMaximum(taskCount);
                        cpb.setTotal(taskCount);
                    };

                    Runnable funcPerTaskDone = () -> {
                        progressBar.setValue(progressBar.getValue() + 1);
                        progressBar.setString(String.format("任务进度(%d/%d): %s", progressBar.getValue(), progressBar.getMaximum(),cpb.iterate()));
                    };

                    Consumer<String> doneComsumer = (String msg) -> {
                        progressBar.setValue(progressBar.getMaximum());
                        JOptionPane.showMessageDialog(this, msg);
                    };

                    var taskExcutor=new TaskExecutor(gtc,entry,setTaskCountBeforeExcutionComsumer,funcPerTaskDone,doneComsumer);
                    taskExcutor.excuteAsync();
                } catch (Exception ex) {
                    log.error("任务执行失败", ex);
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });
    }
}
