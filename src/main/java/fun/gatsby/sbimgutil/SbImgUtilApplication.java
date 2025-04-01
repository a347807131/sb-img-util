package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.utils.ConsoleProgress;
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

        ConsoleProgress cpb= new ConsoleProgress();
        Runnable funcPerTaskDone = () -> {
            String progress = String.format("任务进度: %s", cpb.iterate());
            log.info(progress);
        };

        TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(appConfig.getTaskToStartup());
        TaskExecutor executor = new TaskExecutor(
                appConfig.getGlobalTaskConfig(),
                Map.entry(taskTypeEnum, appConfig.getProcessTasks().get(taskTypeEnum.name())),
                funcPerTaskDone
        );
        cpb.setTotal(executor.getTaskCount());
        executor.excute();
        ctx.stop();
    }

}
