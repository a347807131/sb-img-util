package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.ui.SwingApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.awt.*;

@Slf4j
@SpringBootApplication
public class SbImgUtilApplication {

    public static ApplicationContext ctx;

    public static void main(String[] args) throws Exception {

        var ctx = new SpringApplicationBuilder(SbImgUtilApplication.class)
                .headless(false).run(args);

        SbImgUtilApplication.ctx = ctx;
        AppConfig appConfig = ctx.getBean(AppConfig.class);

        EventQueue.invokeLater(() -> {
            var swingApp = new SwingApp(appConfig);
            swingApp.setVisible(true);
        });
    }

}
