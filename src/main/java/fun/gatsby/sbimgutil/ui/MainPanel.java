package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static fun.gatsby.sbimgutil.ui.util.Insertable.*;

@Slf4j
public class MainPanel extends JPanel {
    final
    AppConfig appConfig;

    CommonInputPanel workNumInputPanel;
    private JProgressBar progressBar;

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;

    CommonInputPanel fileNameRegInputPanel;

    private JRadioButton recursiveChooseBtn;
    private JRadioButton enforceChooseBtn;


    public MainPanel(AppConfig appConfig) {
        this.appConfig = appConfig;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        init();
    }

    private void init() {

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
        add(startBtn);

        startBtn.addActionListener(e -> {
            gtc.setFileNameRegex(fileNameRegInputPanel.getValue());
            gtc.setInDirPath(pathInputPanel.getFilePath());
            gtc.setOutDirPath(pathOutPanel.getFilePath());
            gtc.setRecursive(recursiveChooseBtn.isSelected());
            gtc.setEnforce(enforceChooseBtn.isSelected());

            Component component = taskItemTabbedPanel.getSelectedComponent();
            if(component instanceof TaskItemTabbedPanel.ItemPanel itemPanel){
                Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry = itemPanel.getValidProcessTaskEntry();
                try {
                    TaskExecutor taskExcutor = new TaskExecutor(gtc,appConfig.getProcessTasks(), entry.getKey());
                    taskExcutor.excute();
                    JOptionPane.showMessageDialog(this, "任务完成");
                } catch (Exception ex) {
                    JDialog dialog = new JDialog();
                    dialog.setTitle("错误");
                    dialog.add(new JLabel(ex.getMessage()));
                    dialog.setVisible(true);
                }
            }
        });
    }
}
