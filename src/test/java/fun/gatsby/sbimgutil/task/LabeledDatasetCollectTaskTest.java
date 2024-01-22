package fun.gatsby.sbimgutil.task;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LabeledDatasetCollectTaskTest {

    @Test
    void doWork() throws Throwable {
        new LabeledDatasetCollectTask(new File("D:\\原始备份\\temp\\五经五卷-0007\\0007","Label.txt"), Path.of("D:/tempd"),0.9f).doWork();
    }
}