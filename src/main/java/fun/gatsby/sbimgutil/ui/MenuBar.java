package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.config.StaticBeanGet;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MenuBar extends JMenuBar {

    public MenuBar() {
        jbInit();
    }

    private void jbInit() {
        JMenu settingMenu = new JMenu("设置");
        add(settingMenu);
        JMenuItem sftpMenu = settingMenu.add("使用说明");
        sftpMenu.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        JMenuItem logMenu = settingMenu.add("打开日志文件夹");

        logMenu.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AppConfig appConfig = StaticBeanGet.getBean(AppConfig.class);
                var url = appConfig.getLoggingFilePath();
                GuiUtils.openSystemExplorer(MenuBar.this, new File(url));
            }
        });
    }
}
