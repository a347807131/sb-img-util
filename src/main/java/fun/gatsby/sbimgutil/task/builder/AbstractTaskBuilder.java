package fun.gatsby.sbimgutil.task.builder;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.BaseTask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractTaskBuilder<T extends BaseTask> implements TaskBuilder<T>{

    final AppConfig.GlobalTaskConfig globalTaskConfig;
    final Map<String, Object> configMap;


    @Override
    public List<T> build() throws IOException {
        ReflectUtil.newInstance(getTaskClass(), null);
        return null;
    }



    @Override
    public Class<T> getTaskClass() {
        var clazz = getClass();
        var type = TypeUtil.getTypeArgument(clazz, 0);
        return (Class<T>) type;
    }
}
