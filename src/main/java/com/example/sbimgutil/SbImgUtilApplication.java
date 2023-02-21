package com.example.sbimgutil;

import com.example.sbimgutil.schedule.Scheduler;
import com.example.sbimgutil.utils.Const;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

@Slf4j
@SpringBootApplication
public class SbImgUtilApplication {

    public static void main(String[] args) {
        var ctx = new SpringApplicationBuilder(SbImgUtilApplication.class)
                .headless(false).run(args);
        ProcessExcutor bean = ctx.getBean(ProcessExcutor.class);
        bean.excute();
    }
}
