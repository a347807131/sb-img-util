package com.example.sbimgutil.ui;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.Const;

import javax.swing.*;

public class WorkItemPanel extends JPanel {

    private final CommonInputPanel fileNameRegInputPanel;
//    private final CommonInputPanel formateInputPanel;

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;
    private FilePathInputPanel cataDirInputPanel;
    private FilePathInputPanel blurImgFileInputPanel;
    JComboBox<String> comboBox;

    WorkItemPanel(AppConfig.ProcessTask processTask) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        pathInputPanel = new FilePathInputPanel("输入文件夹");
        pathInputPanel.setFilePath(processTask.getInDirPath());

        pathOutPanel = new FilePathInputPanel("输出文件夹");
        pathOutPanel.setFilePath(processTask.getOutDirPath());

        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹");
        cataDirInputPanel.setFilePath(processTask.getCataDirPath());

        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", JFileChooser.FILES_ONLY);
        blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());

        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", processTask.getFileNameRegex(), 10);


        JPanel formatPanel = new JPanel();
        JLabel formatLabel = new JLabel("目标格式");
        comboBox = new JComboBox<>(Const.SUPORTTED_FORMATS.toArray(new String[]{}));
        formatPanel.add(formatLabel);
        formatPanel.add(comboBox);


        add(pathInputPanel);
        add(pathOutPanel);
        add(cataDirInputPanel);
        add(fileNameRegInputPanel);
        add(blurImgFileInputPanel);
        add(formatPanel);
    }

    public AppConfig.ProcessTask getProcessTask() {
        AppConfig.ProcessTask processTask = new AppConfig.ProcessTask();
        processTask.setInDirPath(pathInputPanel.getFilePath());
        processTask.setOutDirPath(pathOutPanel.getFilePath());
        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
        processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
        processTask.setFileNameRegex(fileNameRegInputPanel.getValue());
        processTask.setFormat(comboBox.getSelectedItem().toString());

        processTask.setEnable(true);
        return processTask;
    }

}