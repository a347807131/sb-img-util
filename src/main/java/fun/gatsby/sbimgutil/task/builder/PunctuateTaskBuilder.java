package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.PunctuateTask;

import java.util.Map;

public class PunctuateTaskBuilder extends AbstractTaskBuilder<PunctuateTask>{
    public PunctuateTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }
}
