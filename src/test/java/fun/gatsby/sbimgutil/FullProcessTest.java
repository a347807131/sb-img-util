package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@Slf4j
@SpringBootTest
public class FullProcessTest {
    @Autowired
    AppConfig appConfig;

    @Test
    public void t1() throws IOException {
        File file = new File("temp.txt");
        for (int i = 0; i < 10; i++) {
            FileUtils.writeStringToFile(file,"test\n","utf-8",true);
        }
    }

}