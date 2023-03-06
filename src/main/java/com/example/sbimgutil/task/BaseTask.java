package com.example.sbimgutil.task;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.example.sbimgutil.schedule.ITask;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public abstract class BaseTask implements ITask {

    protected Logger log = LoggerFactory.getLogger(BaseTask.class);

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

    public void setTaskAfter(Runnable taskAfter) {
        this.taskAfter = taskAfter;
    }

    public void setTaskBefore(Runnable taskBefore) {
        this.taskBefore = taskBefore;
    }

    @Override
    public void after(){
        outFile.renameTo(new File(outFile.getParentFile(),outFile.getName().substring(0,outFile.getName().lastIndexOf("."))));
        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务完成:{},执行时间：{}s",taskName,between);
        state = TaskStateEnum.TERMINATED;
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务执行异常",e);
    }

    @Override
    public void doWork() throws IOException {

    }

    public TaskStateEnum getState() {
        return state;
    }

    public void setState(TaskStateEnum state) {
        this.state = state;
    }
}
