package com.example.sbimgutil.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

    }

    @Override
    public synchronized void beforeFirstStart() {
        startDateTime = LocalDateTime.now();
    }

    @Override
    public void afterAllDone() {
        long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
        String hms = ConsoleProgressBar.genHMS(between);
        log.info("任务组: {} 执行完毕，总共耗时: {}, 单个任务耗时: {} s",name, hms,between/this.size());
    }
}
