package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class SwingApp extends JFrame {

    final
    AppConfig appConfig;

    final
    MainPanel mainPanel;

    public SwingApp(AppConfig appConfig) {
        this.appConfig = appConfig;
        mainPanel = new MainPanel(appConfig);
        String title = "%s ver %s, author: %s".formatted(
                appConfig.getName(),
                appConfig.getVersion(),
                appConfig.getAuthor()
        );
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        add(mainPanel);
    }
}
