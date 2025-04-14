package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.ImageTransformTask;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class ImageTransformTaskBuilder extends AbstractTaskBuilder<ImageTransformTask>{
    public ImageTransformTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        super(gtc, configMap);
    }

    @Override
    public Set<String> getSupportedExts() {
       return Set.of(
                "jpg","jp2","tif","png"
        );
    }
}
