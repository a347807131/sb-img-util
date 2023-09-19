package fun.gatsby.sbimgutil.task;


import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.TaskStateEnum;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Slf4j
public abstract class BaseTask implements ITask {

    //bugfixed
    static {
        ImageIO.getImageWritersByFormatName("jpeg2000").next();
    }

    private LocalDateTime startDate;
    protected String name;
    protected TaskStateEnum state = TaskStateEnum.NEW;
    protected File outFile;

    @Override
    public void before() throws IOException {
        if (outFile != null) {
            outFile = new File(outFile.getParentFile(), outFile.getName() + ".tmp");
            if (outFile.exists()) {
                Files.delete(outFile.toPath());
            }
            if (!outFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outFile);
            }
        }

        state = TaskStateEnum.RUNNING;
        startDate = LocalDateTime.now();
    }


    @Override
    public void after() {
        if (outFile != null && outFile.exists()) {
            String fileName = outFile.getName().substring(0, outFile.getName().lastIndexOf("."));
            File finalFile = new File(outFile.getParentFile(), fileName);
            if(finalFile.exists()){
                finalFile.delete();
            }
            outFile.renameTo(finalFile);
        }

        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务完成:[{}] ,执行耗时：{}s", name, between);
        state = TaskStateEnum.FINISHED;

        ConsoleProgressBar progressBar = TaskExecutor.getGlobalConsoleProgressBar();
        if (progressBar != null) {
            progressBar.iterate();
        }
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务"+name+"执行异常",e);
    }

    @Override
    public String toString() {
        return name;
    }
}
