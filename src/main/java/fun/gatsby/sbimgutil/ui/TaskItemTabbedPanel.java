package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.ImageTransformTask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;
import fun.gatsby.sbimgutil.utils.Const;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TaskItemTabbedPanel extends JTabbedPane {


    private final Map<String, AppConfig.ProcessTask> taskMap;

    TaskItemTabbedPanel(Map<String, AppConfig.ProcessTask> taskMap){
        this.taskMap=taskMap!=null? taskMap:Map.of();
        GuiUtils.setPreferredHeight(this,50);
        JBInit();
    }

    private void JBInit() {

        for (TaskTypeEnum typeEnum : Const.ENABLED_TASK_TYPES) {
            AppConfig.ProcessTask processTask = taskMap.getOrDefault(typeEnum.name(),new AppConfig.ProcessTask());
            add(typeEnum.taskCnName, typeEnum.newTaskItemTabbedPanel(processTask));
        }
    }

    public abstract static class ItemPanel extends JPanel {
        public abstract Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> getCurrentProcessTaskEntry();
    }
}
