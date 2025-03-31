package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootApplication
public class SbImgUtilApplication  {

    public static ApplicationContext ctx;

    public static void main(String[] args) throws Exception {

        var ctx = new SpringApplicationBuilder(SbImgUtilApplication.class)
                .headless(false).run(args);

        SbImgUtilApplication.ctx = ctx;
        AppConfig appConfig = ctx.getBean(AppConfig.class);


        TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(appConfig.getTaskToStartup());
        TaskExecutor executor = new TaskExecutor(
                appConfig.getGlobalTaskConfig(),
                Map.entry(taskTypeEnum, appConfig.getProcessTasks().get(taskTypeEnum.name()))
        );
            try {
                executor.excute();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
//            var swingApp = new SwingApp(appConfig);
//            swingApp.setVisible(true);
    }

}
