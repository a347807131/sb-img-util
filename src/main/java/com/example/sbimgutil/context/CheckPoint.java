package com.example.sbimgutil.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CheckPoint {

    private static File checkPointFile;
    private final FileFilter tifFileFilter;
    private final FileFilter SectionDirFilter;
    private Set<String> finishedValues=new HashSet<>();



    public CheckPoint(File baseOutFile) throws IOException {
        checkPointFile = new File(baseOutFile, "temp.txt");
        if (!checkPointFile.exists()) {
            FileUtils.forceMkdirParent(checkPointFile);
            checkPointFile.createNewFile();
        }

        finishedValues.addAll(FileUtils.readLines(checkPointFile));
         this.tifFileFilter= new FileFilter() {
             @Override
             public boolean accept(File file) {
                 if(file.isDirectory()) return true;
                 File sectionDir = file.getParentFile();
                 String v = sectionDir.getParentFile().getName() + File.separator + sectionDir.getName();
                 return
                         (file.getName().endsWith("tiff")||file.getName().endsWith("tif"))
                         && !finishedValues.contains(v);

             }
         };

        this.SectionDirFilter=new FileFilter() {
            @Override
            public boolean accept(File sectionDir) {
                String v = sectionDir.getParentFile().getName() + File.separator + sectionDir.getName();
                return
                        sectionDir.isDirectory() &&
                        !finishedValues.contains(v);
            }
        };

    }



    public FileFilter getSectionDirFilter() {
        return SectionDirFilter;
    }

    public FileFilter getTifFileFilter() {
        return tifFileFilter;
    }

    void getFinishedBookSectionDirNames() throws IOException {
        Collection<String> finishedValues = FileUtils.readLines(checkPointFile, Charset.defaultCharset());
        this.finishedValues = new HashSet<>(finishedValues);
    }

    public void saveCheckPoint(File sectionDir) {
        try {
            String dataToSave = sectionDir.getParentFile().getName() + File.separator + sectionDir.getName();
            FileUtils.writeStringToFile(checkPointFile, dataToSave+"\n", true);
        } catch (IOException e) {
            log.error("保存检查点失败",e);
            throw new RuntimeException(e);
        }
    }
}
