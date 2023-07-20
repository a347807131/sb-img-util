package fun.gatsby.sbimgutil.ui;

import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class SwingApp extends JFrame {

    final
    MainPanel mainPanel;

    public SwingApp(MainPanel mainPanel) {
        super("SwingApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        this.mainPanel = mainPanel;
        add(mainPanel);
    }

}
