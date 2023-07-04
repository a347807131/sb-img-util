package com.example.sbimgutil.ui;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.utils.Const;

import javax.swing.*;

public class WorkItemPanel extends JPanel {

    private final JPanel formatChosePanel;
    private FilePathInputPanel cataDirInputPanel;
    private FilePathInputPanel blurImgFileInputPanel;


    WorkItemPanel(AppConfig.ProcessTask processTask) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        cataDirInputPanel = new FilePathInputPanel("pdf目录所在文件夹");
//        cataDirInputPanel.setFilePath(processTask.getCataDirPath());

        blurImgFileInputPanel = new FilePathInputPanel("水印文件位置", 20, JFileChooser.FILES_ONLY);
//        blurImgFileInputPanel.setFilePath(processTask.getBlurImagePath());


        formatChosePanel = new JPanel();
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

        cataDirInputPanel.setVisible(false);
        blurImgFileInputPanel.setVisible(false);
        formatChosePanel.setVisible(false);
    }
}