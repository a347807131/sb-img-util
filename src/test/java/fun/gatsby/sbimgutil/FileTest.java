package fun.gatsby.sbimgutil;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public class FileTest {
    @Test
    public void t1() {

        File file = new File("D:");

        log.debug("{}", file.exists());
    }

    @Test
    public void t2() {
        Path path = Path.of("C:/", "/a/b", "b");
        System.out.println(path);
    }
}
