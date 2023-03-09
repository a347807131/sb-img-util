package com.example.sbimgutil.ui;

import javax.swing.*;

public class FilePathInputPanel extends JPanel {

    private final JTextField textField = new JTextField();
    private final JLabel label = new JLabel("File Path");
    private final JButton button = new JButton("选择文件");
    private final int mode;

    public FilePathInputPanel(String labelText, int mode) {
        super();
        this.label.setText(labelText);
        this.mode = mode;
        init();
    }

    public FilePathInputPanel(String labelText) {
        super();
        this.label.setText(labelText);
        this.mode = JFileChooser.DIRECTORIES_ONLY;
        init();
    }

    private void init() {
        add(label);
        textField.setColumns(20);
        add(textField);
        add(button);

        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(mode);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
    }

    public String getFilePath() {
        return textField.getText();
    }

    public void setFilePath(String filePath) {
        textField.setText(filePath);
    }
}
