package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.task.FiveBackspaceReplaceTask;
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


    @Test

    public void t3(){
        File in = new File("D:\\原始备份\\New folder\\5 目录\\27030151\\0001.txt");
        File out = new File("out.txt");
        FiveBackspaceReplaceTask fiveBackspaceReplaceTask = new FiveBackspaceReplaceTask(in,out);
        fiveBackspaceReplaceTask.run();
    }
}
