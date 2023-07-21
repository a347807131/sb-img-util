package fun.gatsby.sbimgutil.ui;

import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class SwingApp extends JFrame {

    final
    MainPanel mainPanel;

    public SwingApp(MainPanel mainPanel) {
        super("图片批处理程序 author: Gatsby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        this.mainPanel = mainPanel;
        add(mainPanel);
    }

}
