package fun.gatsby.sbimgutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatchDownloaderExample {
    
    public static void main(String[] args) {
        // 示例1：基础批量下载
        System.out.println("=== 示例1：基础批量下载 ===");
        List<String> urls = Arrays.asList(
        "https://file.defeisys.com/579457458373672963/DFCLOUD_UNSIGNED_REPORT/2026/01/15/d5dd1fba1_BG-2025-XCJ-3605(Original)_1.pdf",
        "https://file.defeisys.com/579457458373672963/DFCLOUD_UNSIGNED_REPORT/2026/03/16/8e2a27b76_BG-2025-XCJ-3606(Original)_1.pdf",
        "https://file.defeisys.com/579457458373672963/DFCLOUD_UNSIGNED_REPORT/2026/01/15/cb84da8e5_BG-2025-XCJ-3607(Original)_1.pdf"

        );
        
        BatchDownloader.DownloadResult result1 = BatchDownloader.downloadBatch(urls, "C:/Downloads", 3);
        result1.printReport();
        
        // 示例2：带进度回调的批量下载
        System.out.println("\n=== 示例2：带进度回调 ===");
        BatchDownloader.DownloadResult result2 = BatchDownloader.downloadBatchWithProgress(
            urls, 
            "C:/Downloads", 
            3,
            progress -> System.out.println(progress)
        );
        System.out.println(result2);
        
        // 示例3：从文件读取URL列表
        System.out.println("\n=== 示例3：从文件读取 ===");
        try {
            BatchDownloader.DownloadResult result3 = BatchDownloader.downloadFromFile(
                "urls.txt", 
                "C:/Downloads", 
                5
            );
            result3.printReport();
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
        }
        
        // 示例4：增强版批量下载
        System.out.println("\n=== 示例4：增强版批量下载 ===");
        AdvancedBatchDownloader advanced = new AdvancedBatchDownloader("C:/Downloads")
            .setConcurrent(5)
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