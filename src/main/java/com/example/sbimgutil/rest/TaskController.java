package com.example.sbimgutil.rest;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.context.TaskExcutor;
import com.example.sbimgutil.rest.dto.IResult;
import com.example.sbimgutil.rest.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

//@Controller
public class TaskController {

    @Autowired
    private final AppConfig appConfig;

    @Autowired
    private final TaskExcutor taskExcutor;


    public TaskController(AppConfig appConfig, TaskExcutor taskExcutor) {
        this.appConfig = appConfig;
        this.taskExcutor = taskExcutor;
    }


    public IResult getAppConfig() {
        return R.ok(appConfig);
    }

    public IResult getProgress() {

        return null;
    }
}
