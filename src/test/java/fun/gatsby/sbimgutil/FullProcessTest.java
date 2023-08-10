package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExcutor;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active=dev")
public class FullProcessTest {
    @Autowired
    AppConfig appConfig;

    @Test
    public void t1() throws IOException, ExecutionException, InterruptedException {
        TaskExcutor excutor = new TaskExcutor(new AppConfig.ProcessTask(), TaskTypeEnum.BOOK_IMAGE_FIX,appConfig.getGlobalTaskConfig());
        excutor.excute();
    }
}