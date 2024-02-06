package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.utils.ConsoleProgress;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@Slf4j
public class MainPanel extends JPanel {
    final
    AppConfig appConfig;
    final JProgressBar progressBar=new JProgressBar();
    public MainPanel(AppConfig appConfig) {
        this.appConfig = appConfig;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JbInit();
    }

    void JbInit() {
        TaskItemTabbedPanel taskItemTabbedPanel = new TaskItemTabbedPanel(appConfig.getProcessTasks());
        add(taskItemTabbedPanel);
        GlobalConfigPanel globalConfigPanel = new GlobalConfigPanel(appConfig.getGlobalTaskConfig());
        add(globalConfigPanel);

        JButton startBtn = new JButton("开始");
        add(startBtn, "newline, skip 3, gap 15lp 0lp 10lp 10lp, alignx trailing");
        progressBar.setString("任务进度");
        progressBar.setStringPainted(true);
        add(progressBar);

        startBtn.addActionListener(e -> {
            AppConfig.GlobalTaskConfig gtc = globalConfigPanel.getValidGlobalConfig();
            Component component = taskItemTabbedPanel.getSelectedComponent();
            TaskItemTabbedPanel.ItemPanel itemPanel = (TaskItemTabbedPanel.ItemPanel) component;
            Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry = itemPanel.getCurrentProcessTaskEntry();
            new Thread(() -> {
                progressBar.setValue(0);
                progressBar.setString("处理中");
                ConsoleProgress cpb= new ConsoleProgress();

                Runnable funcPerTaskDone = () -> {
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString(String.format("任务进度(%d/%d): %s", progressBar.getValue(), progressBar.getMaximum(),cpb.iterate()));
                };
                startBtn.setEnabled(false);
                try {
                    var taskExcutor=new TaskExecutor(gtc,entry,funcPerTaskDone);
                    int taskCount = taskExcutor.getTaskCount();
                    progressBar.setMaximum(taskCount);
                    cpb.setTotal(taskCount);

                    taskExcutor.excute();
                    JOptionPane.showMessageDialog(MainPanel.this, "任务执行完毕");
                } catch (Exception ex) {
                    log.error("任务执行失败", ex);
                    JOptionPane.showMessageDialog(MainPanel.this, ex.getMessage());
                }finally {
                    progressBar.setValue(0);
                    progressBar.setString("任务进度");
                    startBtn.setEnabled(true);
                }
            }).start();
        });
    }
}
