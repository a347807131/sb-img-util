package fun.gatsby.sbimgutil;

import java.util.List;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 批量文件下载工具类
 */
public class BatchDownloader {
    
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * 批量下载文件（简单版本）
     * @param urls 下载地址列表
     * @param saveDir 保存目录
     * @return 下载结果统计
     */
    public static DownloadResult downloadBatch(List<String> urls, String saveDir) {
        return downloadBatch(urls, saveDir, 3);
    }
    
    /**
     * 批量下载文件（带并发控制）
     * @param urls 下载地址列表
     * @param saveDir 保存目录
     * @param concurrent 并发数
     * @return 下载结果统计
     */
    public static DownloadResult downloadBatch(List<String> urls, String saveDir, int concurrent) {
        DownloadResult result = new DownloadResult();
        result.total = urls.size();
        
        // 创建保存目录
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(concurrent);
        CountDownLatch latch = new CountDownLatch(urls.size());
        List<Future<DownloadTaskResult>> futures = new ArrayList<>();
        
        // 提交所有下载任务
        for (String url : urls) {
            String filename = getFilenameFromUrl(url);
            String filepath = saveDir + File.separator + filename;
            
            DownloadTask task = new DownloadTask(url, filepath);
            futures.add(executor.submit(() -> {
                try {
                    DownloadTaskResult taskResult = task.download();
                    return taskResult;
                } finally {
                    latch.countDown();
                }
            }));
        }
        
        // 等待所有任务完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 收集结果
        for (Future<DownloadTaskResult> future : futures) {
            try {
                DownloadTaskResult taskResult = future.get();
                if (taskResult.success) {
                    result.success++;
                    result.successFiles.add(taskResult.filename);
                } else {
                    result.failed++;
                    result.failedFiles.add(taskResult.filename);
                    result.errors.put(taskResult.filename, taskResult.error);
                }
            } catch (Exception e) {
                result.failed++;
                result.errors.put("unknown", e.getMessage());
            }
        }
        
        executor.shutdown();
        return result;
    }
    
    /**
     * 批量下载文件（带进度回调）
     * @param urls 下载地址列表
     * @param saveDir 保存目录
     * @param concurrent 并发数
     * @param progressCallback 进度回调
     * @return 下载结果统计
     */
    public static DownloadResult downloadBatchWithProgress(
            List<String> urls, 
            String saveDir, 
            int concurrent,
            Consumer<ProgressInfo> progressCallback) {
        
        DownloadResult result = new DownloadResult();
        result.total = urls.size();
        
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrent);
        CountDownLatch latch = new CountDownLatch(urls.size());
        AtomicInteger completedCount = new AtomicInteger(0);
        
        for (String url : urls) {
            String filename = getFilenameFromUrl(url);
            String filepath = saveDir + File.separator + filename;
            
            executor.submit(() -> {
                try {
                    DownloadTask task = new DownloadTask(url, filepath);
                    DownloadTaskResult taskResult = task.download();
                    
                    if (taskResult.success) {
                        result.success++;
                        result.successFiles.add(taskResult.filename);
                    } else {
                        result.failed++;
                        result.failedFiles.add(taskResult.filename);
                        result.errors.put(taskResult.filename, taskResult.error);
                    }
                    
                    int completed = completedCount.incrementAndGet();
                    if (progressCallback != null) {
                        ProgressInfo info = new ProgressInfo();
                        info.completed = completed;
                        info.total = urls.size();
                        info.success = result.success;
                        info.failed = result.failed;
                        info.currentFile = filename;
                        info.percent = (completed * 100) / urls.size();
                        progressCallback.accept(info);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        return result;
    }
    
    /**
     * 从文件读取URL列表并批量下载
     * @param urlFile URL列表文件（每行一个URL）
     * @param saveDir 保存目录
     * @param concurrent 并发数
     * @return 下载结果统计
     */
    public static DownloadResult downloadFromFile(String urlFile, String saveDir, int concurrent) 
            throws IOException {
        List<String> urls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(urlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && (line.startsWith("http://") || line.startsWith("https://"))) {
                    urls.add(line);
                }
            }
        }
        return downloadBatch(urls, saveDir, concurrent);
    }
    
    /**
     * 从URL获取文件名
     */
    private static String getFilenameFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            String filename = parts[parts.length - 1];
            if (filename == null || filename.isEmpty() || !filename.contains(".")) {
                filename = "file_" + System.currentTimeMillis() + ".download";
            }
            // 去除URL参数
            int queryIndex = filename.indexOf('?');
            if (queryIndex > 0) {
                filename = filename.substring(0, queryIndex);
            }
            return filename;
        } catch (Exception e) {
            return "file_" + System.currentTimeMillis() + ".download";
        }
    }
    
    // 下载任务类
    static class DownloadTask {
        private String url;
        private String filepath;
        
        public DownloadTask(String url, String filepath) {
            this.url = url;
            this.filepath = filepath;
        }
        
        public DownloadTaskResult download() {
            DownloadTaskResult result = new DownloadTaskResult();
            result.url = url;
            result.filename = new File(filepath).getName();
            
            HttpURLConnection connection = null;
            try {
                URL urlObj = new URL(url);
                connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(DEFAULT_TIMEOUT);
                connection.setReadTimeout(DEFAULT_TIMEOUT);
                connection.setRequestProperty("User-Agent", 
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(filepath)) {
                        
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        
                        result.success = true;
                        result.fileSize = new File(filepath).length();
                    }
                } else {
                    result.success = false;
                    result.error = "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                result.success = false;
                result.error = e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            
            return result;
        }
    }
    
    // 结果类
    public static class DownloadResult {
        public int total = 0;
        public int success = 0;
        public int failed = 0;
        public List<String> successFiles = new ArrayList<>();
        public List<String> failedFiles = new ArrayList<>();
        public Map<String, String> errors = new HashMap<>();
        
        @Override
        public String toString() {
            return String.format("下载完成 - 总计: %d, 成功: %d, 失败: %d", 
                               total, success, failed);
        }
        
        public void printReport() {
            System.out.println("=".repeat(50));
            System.out.println("批量下载报告");
            System.out.println("=".repeat(50));
            System.out.println("总任务数: " + total);
            System.out.println("成功: " + success);
            System.out.println("失败: " + failed);
            
            if (!successFiles.isEmpty()) {
                System.out.println("\n成功文件:");
                successFiles.forEach(f -> System.out.println("  ✓ " + f));
            }
            
            if (!failedFiles.isEmpty()) {
                System.out.println("\n失败文件:");
                failedFiles.forEach(f -> System.out.println("  ✗ " + f));
            }
            
            if (!errors.isEmpty()) {
                System.out.println("\n错误详情:");
                errors.forEach((file, error) -> System.out.println("  " + file + ": " + error));
            }
        }
    }
    
    static class DownloadTaskResult {
        String url;
        String filename;
        boolean success;
        long fileSize;
        String error;
    }
    
    public static class ProgressInfo {
        public int completed;
        public int total;
        public int success;
        public int failed;
        public String currentFile;
        public int percent;
        
        @Override
        public String toString() {
            return String.format("进度: %d%% (%d/%d) - 成功: %d, 失败: %d - 当前: %s", 
                               percent, completed, total, success, failed, currentFile);
        }
    }
}