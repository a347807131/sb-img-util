package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpDownloader;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class T2 {

    @Test
    public void t1() throws IOException {
        var urls=FileUtil.readLines("C:\\Users\\94712\\IdeaProjects\\sb-img-util\\src\\test\\resources\\urls.txt", "utf-8")
                .stream().filter(e->e.startsWith("http")).toList();
        // 示例4：增强版批量下载
        System.out.println("\n=== 示例4：增强版批量下载 ===");
        AdvancedBatchDownloader advanced = new AdvancedBatchDownloader("D:/Downloads")
                .setConcurrent(10)
                .setRetryCount(3)
                .setResume(true)
                .setSpeedLimit(1024) // 限速1MB/s
                .setProgressCallback(progress -> {
                    System.out.printf("\r进度: %d%% (%d/%d) 速度: %.2f MB/s",
                            progress.percent, progress.completed, progress.total,
                            progress.totalBytes / 1024.0 / 1024.0);
                })
                .setLogCallback(msg -> {
                    if (!msg.startsWith("下载中")) {
                        System.out.println(msg);
                    }
                });

        AdvancedBatchDownloader.DownloadResult result4 = advanced.download(urls);
        System.out.println("\n" + result4);
    }
}
