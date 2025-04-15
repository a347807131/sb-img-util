package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.schedule.ITask;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface TaskBuilder<T extends ITask> {
    /**
     * 根据配置构建任务列表
     */
    List<T> build() throws IOException;

    /**
     * 根据输入的单元文件生成任务
     */
    T build(File inFile) throws IOException;

    Class<T> getTaskClass();

    /**
     * 根据输入单元文件生成对应的输出的文件
     */
    File outFile(File inFile);

    /**
     * 根据输入单元文件和目标后缀生成对应的输出的文件
     */
    File outFile(File inFile, String format);

    /**
     * 载入所有需要处理的文件
     */
    List<File> loadInFiles();

    /**
     * 获取支持的文件后缀
     */
    Set<String> getSupportedExts();

    /**
     * 获取文件过滤器
     */
    IOFileFilter getFileFileter();
}
