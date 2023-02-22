package com.example.sbimgutil;

import com.example.sbimgutil.context.ProcessExcutor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class SbImgUtilApplication {

    public static void main(String[] args) throws Exception {
        var ctx = new SpringApplicationBuilder(SbImgUtilApplication.class)
                .headless(false).run(args);
        ProcessExcutor bean = ctx.getBean(ProcessExcutor.class);
        bean.excute();
    }
}
