package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
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
    }


    @Test
    void testTaskExecutor() throws IOException, ExecutionException, InterruptedException {
    }
}
