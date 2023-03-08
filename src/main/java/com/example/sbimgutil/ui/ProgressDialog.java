package com.example.sbimgutil.ui;

import javax.swing.*;

public class ProgressDialog extends javax.swing.JDialog {

    public ProgressDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void initComponents() {
        setTitle("进度");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        add(progressBar);
    }
}
