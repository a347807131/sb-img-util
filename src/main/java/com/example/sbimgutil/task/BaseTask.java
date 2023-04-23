package com.example.sbimgutil.task;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.example.sbimgutil.context.TaskExcutor;
import com.example.sbimgutil.schedule.ITask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;


@Slf4j
public abstract class BaseTask implements ITask {

    //bugfixed
    static {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg2000");
        ImageWriter writer = writers.next();
    }


    private LocalDateTime startDate;
    protected Runnable taskBefore;
    protected Runnable taskAfter;

    protected String taskName;
    protected TaskStateEnum state = TaskStateEnum.NEW;

    protected File outFile;

    @Override
    public void before() throws IOException {
        outFile=new File(outFile.getParentFile(),outFile.getName() + ".tmp");
        FileUtils.forceMkdirParent(outFile);
        state = TaskStateEnum.RUNNING;
        startDate = LocalDateTime.now();
    }


    @Override
    public void after(){
        outFile.renameTo(new File(outFile.getParentFile(),outFile.getName().substring(0,outFile.getName().lastIndexOf("."))));

        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务完成:{},执行时间：{}s",taskName,between);
        state = TaskStateEnum.TERMINATED;

        TaskExcutor.getGlobalConsoleProgressBar().iterate();
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务执行异常",e);
    }

    @Override
    public void doWork() throws IOException {

    }
}
