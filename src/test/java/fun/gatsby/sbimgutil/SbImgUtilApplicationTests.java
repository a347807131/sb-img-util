package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import jakarta.annotation.PostConstruct;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SpringBootTest(properties = "spring.profiles.active=dev")
class SbImgUtilApplicationTests {

    @Autowired
    AppConfig appConfig;
    @Test
    void contextLoads() {
    }

    AppConfig.ProcessTask processTask=null;

    @PostConstruct
    void init() {
        Map<String, AppConfig.ProcessTask> processTasks = appConfig.getProcessTasks();
        processTask= appConfig.getProcessTasks().get(TaskTypeEnum.IMAGE_TRANSFORM.name());
    }


    @Test
    void testTaskExecutor() throws IOException, ExecutionException, InterruptedException {
        AppConfig.GlobalTaskConfig gtc = appConfig.getGlobalTaskConfig();
        TaskExecutor excutor = new TaskExecutor(gtc,processTask,
//                TaskTypeEnum.FIVE_BACKSPACE_REPLACE
//                TaskTypeEnum.IMAGE_COMPRESS
//                TaskTypeEnum.IMAGE_TRANSFORM
                TaskTypeEnum.PDF_MERGE
//                TaskTypeEnum.BOOK_IMAGE_FIX
        );
        excutor.excute();
    }
}
