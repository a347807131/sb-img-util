package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.Jp2CompressTask;

import java.util.Map;

public class Jp2CompressTaskBuilder extends AbstractTaskBuilder<Jp2CompressTask>{
    public Jp2CompressTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }
}
