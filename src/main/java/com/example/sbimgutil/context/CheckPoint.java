package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class CheckPoint {

    private static File checkPointFile;
    private final FileFilter volumeDirFilter;
    private Set<String> finishedValues=new HashSet<>();



    public CheckPoint(File baseOutFile) throws IOException {
        checkPointFile = new File(baseOutFile, "temp.txt");
        if (!checkPointFile.exists()) {
            FileUtils.forceMkdirParent(checkPointFile);
            checkPointFile.createNewFile();
        }

        finishedValues.addAll(FileUtils.readLines(checkPointFile));
        this.volumeDirFilter=new FileFilter() {
            @Override
            public boolean accept(File sectionDir) {
                String v = sectionDir.getParentFile().getName() + File.separator + sectionDir.getName();
                return
                        sectionDir.isDirectory() &&
                        !finishedValues.contains(v);
            }
        };

    }

    public void saveCheckPoint(File volumeDir,int code) {
        try {
            String dataToSave = String.format("%s/%s,%s\n",
                    volumeDir.getParentFile().getName(),
                    volumeDir.getName(),
                    code
            );
            FileUtils.writeStringToFile(checkPointFile, dataToSave, UTF_8,true);
        } catch (IOException e) {
            log.error("保存检查点失败",e);
            throw new RuntimeException(e);
        }
    }

    public boolean checkIfFinished(File volumeDir, int code) {
        String v = String.format("%s/%s,%s",
                volumeDir.getParentFile().getName(),
                volumeDir.getName(),
                code
        );
        return finishedValues.contains(v);
    }
}
