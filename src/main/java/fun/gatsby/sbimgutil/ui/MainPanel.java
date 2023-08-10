package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;

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
    private FilePathInputPanel labelFileInputPanel;


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
        JLabel logLabel = new JLabel("<HTML><U>打开日志文件夹</U></HTML>");
        logLabel.setForeground(Color.BLUE);
        logLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(logLabel);
        logLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String url = appConfig.getLoggingFilePath();
                try {
                    Runtime.getRuntime().exec("cmd /c explorer " + url);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        AppConfig.GlobalTaskConfig gtc = appConfig.getGlobalTaskConfig();


        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(gtc.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", gtc.getFileNameRegex(), 10);
        recursiveChooseBtn = new JRadioButton("递归处理", gtc.isRecursive());

        JPanel nameRegAndWokerNumWrapperPanel = new JPanel();
        nameRegAndWokerNumWrapperPanel.setLayout(new BoxLayout(nameRegAndWokerNumWrapperPanel, BoxLayout.X_AXIS));
        nameRegAndWokerNumWrapperPanel.add(fileNameRegInputPanel);
        nameRegAndWokerNumWrapperPanel.add(workNumInputPanel);
        nameRegAndWokerNumWrapperPanel.add(recursiveChooseBtn);
        add(nameRegAndWokerNumWrapperPanel);


        JPanel pathWrapperPanel = new JPanel();
        pathWrapperPanel.setLayout(new BoxLayout(pathWrapperPanel, BoxLayout.X_AXIS));

        pathInputPanel = new FilePathInputPanel("输入文件夹", 10);
        pathInputPanel.setFilePath(gtc.getInDirPath());
        pathOutPanel = new FilePathInputPanel("输出文件夹", 10);
        pathOutPanel.setFilePath(gtc.getOutDirPath());
        pathWrapperPanel.add(pathInputPanel);
        pathWrapperPanel.add(pathOutPanel);
        add(pathWrapperPanel);

        taskItemChoosePanel = new WorkItemChoosePanel();
        add(taskItemChoosePanel);


        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹", 10);
        cataDirInputPanel.setFilePath(appConfig.getProcessTasks().get(TaskTypeEnum.PDF_MERGE.name()).getCataDirPath());

        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 10, JFileChooser.FILES_ONLY);
        blurImgFileInputPanel.setFilePath(appConfig.getProcessTasks().get(TaskTypeEnum.DRAW_BLUR.name()).getBlurImagePath());

        labelFileInputPanel = new FilePathInputPanel("裁切标注文件位置", 10, JFileChooser.FILES_ONLY);
        labelFileInputPanel.setFilePath(appConfig.getProcessTasks().get(TaskTypeEnum.IMAGE_CUT.name()).getLabelFilePath());
        add(labelFileInputPanel);

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
        labelFileInputPanel.setVisible(false);


        JButton startBtn = new JButton("开始");
        add(startBtn);

        taskItemChoosePanel.addItemListener(e -> {
            formatChosePanel.setVisible(false);
            cataDirInputPanel.setVisible(false);
            blurImgFileInputPanel.setVisible(false);
            labelFileInputPanel.setVisible(false);

            String actionCommand = e.getActionCommand();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.parse(actionCommand);
            Objects.requireNonNull(taskTypeEnum, "taskType is null");
            JPanel tagetPanel = switch (taskTypeEnum) {
                case IMAGE_TRANSFORM -> formatChosePanel;
                case PDF_MERGE -> cataDirInputPanel;
                case IMAGE_COMPRESS -> null;
                case IMAGE_CUT -> labelFileInputPanel;
                case DRAW_BLUR -> blurImgFileInputPanel;
                default -> null;
            };
            if (tagetPanel != null)
                tagetPanel.setVisible(true);
        });

        startBtn.addActionListener(e -> {

            AppConfig.ProcessTask processTask = new AppConfig.ProcessTask();
            gtc.setFileNameRegex(fileNameRegInputPanel.getValue());
            gtc.setInDirPath(pathInputPanel.getFilePath());
            String outDirPath = pathOutPanel.getFilePath();
            gtc.setOutDirPath(outDirPath);
            gtc.setRecursive(recursiveChooseBtn.isSelected());

            TaskTypeEnum taskType = taskItemChoosePanel.getSelectedTaskType();
            switch (taskType) {
                case DRAW_BLUR -> processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
                case PDF_MERGE -> processTask.setCataDirPath(cataDirInputPanel.getFilePath());
                case IMAGE_CUT -> processTask.setLabelFilePath(labelFileInputPanel.getFilePath());
                case IMAGE_TRANSFORM -> processTask.setFormat((String) formatComboBox.getSelectedItem());
            }
            int maxWorkerNum = Integer.parseInt(workNumInputPanel.getValue());
            gtc.setMaxWorkerNum(maxWorkerNum);
            try {
                TaskExecutor taskExcutor = new TaskExecutor(gtc,processTask, taskType);
                taskExcutor.excute();
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
