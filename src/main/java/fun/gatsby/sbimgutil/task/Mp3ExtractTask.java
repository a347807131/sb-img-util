package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Mp3ExtractTask extends BaseTask{
    public Mp3ExtractTask(File inFile, File outFile, Map<String,Object> configMap) {
        super(inFile, outFile, configMap);
    }
    private static final String CMDFORMATSTR="ffmpeg -i \"%s\" -q:a 0 \"%s\"";

    @Override
    public void before() throws IOException {
    }

    /**
     * ffmpeg -i input_video.mp4 -q:a 0 output_audio.mp3
     * @throws Throwable
     */
    @Override
    public void doWork() throws Throwable {
        if(outFile.exists()) {
            log.info("{}的结果文件已存在,跳过",inFile);
            return;
        }
        super.before();

        var cmd=CMDFORMATSTR.formatted(inFile.getAbsolutePath(),outFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c",cmd);
        Process process = pb.start();

        //超时检查
        if(!process.waitFor(10, TimeUnit.MINUTES)){
            throw new RuntimeException("ffmpeg process timeout 10m,[%s]".formatted(inFile.getAbsolutePath()));
        }
        process.destroyForcibly();
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        return name;
    }

    static List<String> EXTENSIONS = List.of("mp4","avi","mkv","wmv","flv","rmvb","rm","mov","mpg","mpeg","m4v","3gp","ts","rmi");
}
