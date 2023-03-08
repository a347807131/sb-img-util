package com.example.sbimgutil.ui;

import javax.swing.*;

public class CommonInputPanel extends JPanel {

    JTextField textField = new JTextField();
    JLabel label = new JLabel("File Path");

    public CommonInputPanel(String labelText, String value) {
        super();
        add(label);
        add(textField);
        textField.setColumns(5);
        label.setText(labelText);
        textField.setText(value);
    }

    public CommonInputPanel(String labelText, String value, int columns) {
        this(labelText, value);
        textField.setColumns(columns);
    }

    public String getValue() {
        return textField.getText();
    }

    public void setValue(String value) {
        textField.setText(value);
    }
}
