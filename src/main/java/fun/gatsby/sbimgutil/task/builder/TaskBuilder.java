package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.schedule.ITask;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface TaskBuilder<T extends ITask> {
    List<T> build() throws IOException;

    T build(File inFile) throws IOException;

    Class<T> getTaskClass();

    File outFile(File inFile);

    File outFile(File inFile, String format);

    List<File> loadInFiles();

    Set<String> getSupportedExts();

    IOFileFilter getFileFileter();
}
