package com.example.sbimgutil;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.context.TaskExcutor;
import com.example.sbimgutil.ui.SwingApp;
import lombok.extern.slf4j.Slf4j;
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
        EventQueue.invokeLater(() -> {
            var ex = ctx.getBean(SwingApp.class);
            ex.setVisible(true);
        });
    }
}
