package fun.gatsby.sbimgutil;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class T2 {

    @Test
    public void t1() throws IOException {


        String jsongString = Files.readString(Path.of("src/test/resources/合同导出.json"));
        List<Stats> stats = JSON.parseArray(jsongString, Stats.class);
        System.out.println();
    }
}
