package fun.gatsby.sbimgutil.ui;

import javax.swing.*;
import java.io.File;

public class FilePathInputPanel extends JPanel {

    private final JTextField textField = new JTextField();
    private final JLabel label = new JLabel("File Path");
    private final JButton button = new JButton("选择");
    private final int mode;
    private int columns = 20;
    private JFileChooser fileChooser;

    public FilePathInputPanel(String labelText, int columns) {
        this(labelText, columns, JFileChooser.DIRECTORIES_ONLY);
    }

    public FilePathInputPanel(String labelText, int columns, int mode) {
        super();
        this.label.setText(labelText);
        this.mode = mode;
        this.columns = columns;
        fileChooser = new JFileChooser(labelText);
        init();
    }

    private void init() {
        add(label);
        textField.setColumns(columns);
        add(textField);
        add(button);

        button.addActionListener(e -> {
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
        if(filePath==null)
            return;
        textField.setText(filePath);
        fileChooser.setCurrentDirectory(new File(filePath));
    }
}
