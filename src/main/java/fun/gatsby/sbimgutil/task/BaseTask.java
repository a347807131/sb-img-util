package fun.gatsby.sbimgutil.task;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.TaskStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
public abstract class BaseTask implements ITask {

    //bugfixed
    static {
//        ImageIO.getImageWritersByFormatName("jpeg2000").next();
    }

    protected String name;

    private LocalDateTime startDate;
    protected TaskStateEnum state = TaskStateEnum.NEW;
    protected File outFile;
    protected File inFile;
    protected Map<String,Object> configMap;

    public BaseTask(File inFile, File outFile, Map<String,Object> configMap){
        this.inFile = inFile;
        this.outFile = outFile;
        this.configMap=configMap;
        this.name="%s->%s".formatted(inFile.getName(), outFile.getName());
    }

    static final String TEMP_FILE_PREFIX = "temp.";
    @Override
    public void before() throws IOException {
        if (outFile != null) {
            this.outFile=new File(outFile.getParentFile(), TEMP_FILE_PREFIX+outFile.getName());
            if (outFile.exists()) {
                Files.delete(outFile.toPath());
            }
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
            String fileName = outFile.getName().substring(TEMP_FILE_PREFIX.length());
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
        return "%s->%s".formatted(inFile.getName(), outFile.getName());
    };
}
