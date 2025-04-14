package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.OcrTask;

import java.util.Map;

public class OcrTaskBuilder extends AbstractTaskBuilder<OcrTask>{
    public OcrTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

}
