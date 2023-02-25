package com.example.sbimgutil.task;


import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.commons.schedule.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;


public abstract class BaseTask implements ITask {

    protected Logger log = LoggerFactory.getLogger(BaseTask.class);

    private LocalDateTime startDate;
    protected Runnable taskBefore;
    protected Runnable taskAfter;

    @Override
    public void before() {
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
        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务执行时间：{}s",between);
    }

    @Override
    public void doWork() {

    }
}
