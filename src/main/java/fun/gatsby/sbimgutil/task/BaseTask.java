package fun.gatsby.sbimgutil.task;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.TaskStateEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseTask<C extends Object> implements ITask {

    //bugfixed
    static {
//        ImageIO.getImageWritersByFormatName("jpeg2000").next();
    }

    protected String name;

    private LocalDateTime startDate;
    protected TaskStateEnum state = TaskStateEnum.NEW;
    protected final File inFile;
    protected File outFile;
    @Getter
    protected final C config;

    public BaseTask(File inFile, File outFile,C config) {
        this.inFile = inFile;
        this.config = config;
        this.outFile = outFile;
    }

    static final String TEMP_FILE_PREFIX = ".tmp";
    @Override
    public void before() throws IOException {
        if (outFile != null) {
            if (outFile.exists()) {
                Files.delete(outFile.toPath());
            }
            var tempFile=new File(outFile.getParentFile(), outFile.getName()+TEMP_FILE_PREFIX);
            if (tempFile.exists()) {
                Files.delete(tempFile.toPath());
            }
            outFile.renameTo(tempFile);
            outFile = tempFile;
            if (!outFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outFile);
            }
        }

        state = TaskStateEnum.RUNNING;
        startDate = LocalDateTime.now();
        log.info("开始执行任务:[{}]", getName());
    }

    @Override
    public void run() {
        ITask.super.run();
    }

    @Override
    public void after() {
        if (outFile != null && outFile.exists()) {
            String fileName = outFile.getName().substring(0, outFile.getName().length() - TEMP_FILE_PREFIX.length());
            File finalFile = new File(outFile.getParentFile(), fileName);
            if(finalFile.exists()){
                finalFile.delete();
            }
            outFile.renameTo(finalFile);
        }

        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.info("任务完成:[{}] ,执行耗时：{}s", getName(), between);
        state = TaskStateEnum.FINISHED;
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务"+getName()+"执行异常",e);
    }

    @Override
    public String toString() {
        return getName() + "@" + state.name();
    }

    public String getName(){
        return "%s %s".formatted(getClass().getSimpleName(),inFile.getAbsolutePath());
    };
}
