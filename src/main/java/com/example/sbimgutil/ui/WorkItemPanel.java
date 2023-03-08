package com.example.sbimgutil.ui;

import com.example.sbimgutil.config.AppConfig;

import javax.swing.*;

public class WorkItemPanel extends JPanel {

    private final CommonInputPanel fileNameRegInputPanel;
    private final CommonInputPanel formateInputPanel;

    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;
    private FilePathInputPanel cataDirInputPanel;
    private FilePathInputPanel blurImgFileInputPanel;

    WorkItemPanel(AppConfig.ProcessTask processTask) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        pathInputPanel = new FilePathInputPanel("输入文件夹");
        pathInputPanel.setFilePath(processTask.getInDirPath());

        pathOutPanel = new FilePathInputPanel("输出文件夹");
        pathOutPanel.setFilePath(processTask.getOutDirPath());

        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹");
        cataDirInputPanel.setFilePath(processTask.getCataDirPath());

        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置");
        blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());

        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", processTask.getFileNameRegex(), 10);

        formateInputPanel = new CommonInputPanel("目标格式", processTask.getFormat());


        add(pathInputPanel);
        add(pathOutPanel);
        add(cataDirInputPanel);
        add(fileNameRegInputPanel);
        add(formateInputPanel);
        add(blurImgFileInputPanel);
    }

    public AppConfig.ProcessTask getProcessTask() {
        AppConfig.ProcessTask processTask = new AppConfig.ProcessTask();
        processTask.setInDirPath(pathInputPanel.getFilePath());
        processTask.setOutDirPath(pathOutPanel.getFilePath());
        processTask.setCataDirPath(cataDirInputPanel.getFilePath());
        processTask.setBlurImagePath(blurImgFileInputPanel.getFilePath());
        processTask.setFileNameRegex(fileNameRegInputPanel.getValue());
        processTask.setFormat(formateInputPanel.getValue());

        processTask.setEnable(true);
        return processTask;
    }

}