package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class SwingApp extends JFrame {

    final
    AppConfig appConfig;

    final
    MainPanel mainPanel;

    public SwingApp(MainPanel mainPanel, AppConfig appConfig) {
        String title = "%s author: %s, ver %s".formatted(
                "图片批处理程序", "JGatsby", appConfig.getVersion()
        );
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        this.mainPanel = mainPanel;
        add(mainPanel);
        this.appConfig = appConfig;
    }
}
