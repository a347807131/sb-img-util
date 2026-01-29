package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.DrawBlurTask;

import java.util.Map;


public class DrawBlurTaskBuilder extends AbstractTaskBuilder<DrawBlurTask>{
    public DrawBlurTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }
}
