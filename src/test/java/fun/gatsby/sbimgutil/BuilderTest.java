package fun.gatsby.sbimgutil;

import cn.hutool.core.util.TypeUtil;
import fun.gatsby.sbimgutil.task.Mp3ExtractTask;
import fun.gatsby.sbimgutil.task.builder.Mp3ExtractTaskBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class BuilderTest {

    @Test
    public void t1(){
        Mp3ExtractTaskBuilder builder = new Mp3ExtractTaskBuilder();
        var clazz = builder.getTaskClass();
        Type[] typeArguments = TypeUtil.getTypeArguments(Mp3ExtractTaskBuilder.class);
    }
}
