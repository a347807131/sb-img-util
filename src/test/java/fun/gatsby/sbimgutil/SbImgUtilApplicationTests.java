package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SbImgUtilApplicationTests {

    @Autowired
    AppConfig appConfig;
    @Test
    void contextLoads() {
        System.out.println();
    }

}
