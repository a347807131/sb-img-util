package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.checkerframework.checker.units.qual.C;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Mp3ExtractTask extends BaseTask<Mp3ExtractTask.Config>{

    public record Config(
            Integer timeout
    ){}

    public Mp3ExtractTask(File inFile, File outFile, Config config) {
        super(inFile, outFile, config);
    }
    private static final String CMDFORMATSTR="ffmpeg -i \"%s\" -q:a 0 -f mp3 \"%s\"";

    /**
     * ffmpeg -i input_video.mp4 -q:a 0 -f mp3 output_audio.mp3
     * @throws Throwable
     */
    @Override
    public void doWork() throws Throwable {

        var cmd=CMDFORMATSTR.formatted(inFile.getAbsolutePath(),outFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c",cmd);
//        pb.inheritIO();
        Process process = pb.start();
        var is = process.getErrorStream();
//        fixed 输出占满缓存，导致程序卡死
        // Create BufferedReader to read the output from FFmpeg process
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
        }
        //超时检查
        if(!process.waitFor(config.timeout, TimeUnit.SECONDS)){
            throw new RuntimeException("ffmpeg process timeout {%s},[%s]".formatted(config.timeout,inFile.getAbsolutePath()));
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

    public final static List<String> EXTENSIONS = List.of("mp4","avi","mkv","wmv","flv","rmvb","rm","mov","mpg","mpeg","m4v","3gp","ts","rmi");
}
