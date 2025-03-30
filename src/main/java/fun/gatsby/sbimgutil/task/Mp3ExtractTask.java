package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.*;
import java.util.List;

public class Mp3ExtractTask extends BaseTask{
    File inFile;
    public Mp3ExtractTask(File inFile, File outFile){
        this.inFile=inFile;
        this.outFile=outFile;

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
        int exitCode = process.waitFor();

        // 读取标准输出流
        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        // 读取错误流
        BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
        );

        // 打印输出和错误信息
        String line;
        while ((line = inputReader.readLine()) != null) {
            System.out.println("输出: " + line);
        }
        while ((line = errorReader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待命令执行完成并获取退出码
        System.out.println("退出码: " + exitCode);
        if(exitCode!=0)
            throw new RuntimeException("处理失败");
    }

    public static class TaskGenerator extends BaseTaskGenerator {
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.EXTRACT_SOUNDTRACK_FROM_VIDEO);
        }

        @Override
        public IOFileFilter getFileFileter() {
             var fileterSuper=super.getFileFileter();
            IOFileFilter suffixFileFilter = FileFilterUtils.suffixFileFilter(".mp4");
            return FileFilterUtils.and(fileterSuper,suffixFileFilter);
        }

        @Override
        public List<ITask> generate() throws IOException {
            return super.generate();
        }
    }
}
