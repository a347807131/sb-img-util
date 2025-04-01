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
import java.util.concurrent.TimeUnit;

@Slf4j
public class Mp3ExtractTask extends BaseTask{
    File inFile;
    public Mp3ExtractTask(File inFile, File outFile){
        this.inFile=inFile;
        this.outFile=outFile;

        this.name="音频提取"+inFile.getAbsolutePath();
    }
    private static final String CMDFORMATSTR="ffmpeg -i \"%s\" -q:a 0 \"%s\"";

    /**
     * ffmpeg -i input_video.mp4 -q:a 0 output_audio.mp3
     * @throws Throwable
     */
    @Override
    public void doWork() throws Throwable {
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

    public static class TaskGenerator extends BaseTaskGenerator {
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.EXTRACT_SOUNDTRACK_FROM_VIDEO);
        }

        @Override
        public IOFileFilter getFileFileter() {
             var fileterSuper=super.getFileFileter();
            var extFilter=new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String fileName = file.getName();
                    String ext = FileUtil.extName(file);
                    return EXTENSIONS.contains(ext);
                }
            };
            return FileFilterUtils.and(fileterSuper,
                    FileFilterUtils.asFileFilter(extFilter)
                    );
        }

        @Override
        public List<ITask> generate() throws IOException {
            return super.generate();
        }
    }
}
