package com.example.sbimgutil;

import com.example.sbimgutil.utils.Const;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.stream.Stream;

@SpringBootTest
public class FullProcessTest {
    @Autowired
    ProcessConfig processConfig;

    @Test
    public void t1(){
        String baseDirPath = processConfig.getBaseDirPath();
        File tifDir = new File(baseDirPath, Const.tifDirName);

        FileFilter tifFileFilter = File::isDirectory;
        File[] files = tifDir.listFiles(tifFileFilter);
        if(files==null)
            throw new RuntimeException("目标tif文件夹无数据");

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (File bookDir : files) {
            BookImageDirProcessTask bookImageDirProcessTask = new BookImageDirProcessTask(bookDir,processConfig);
            tasks.add(bookImageDirProcessTask);
        }
        tasks.parallelStream().forEach(Runnable::run);
    }
}
