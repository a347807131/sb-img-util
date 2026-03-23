package fun.gatsby.sbimgutil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 增强版批量下载器 - 支持断点续传、重试、限速
 */
public class AdvancedBatchDownloader {
    
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int BUFFER_SIZE = 8192;
    private static final int DEFAULT_RETRY = 3;
    
    private int concurrent = 3;
    private int retryCount = DEFAULT_RETRY;
    private String saveDir;
    private long speedLimit = 0; // 0表示不限速
    private boolean resume = true; // 是否断点续传
    private Consumer<ProgressInfo> progressCallback;
    private Consumer<String> logCallback;
    
    private volatile boolean paused = false;
    private volatile boolean cancelled = false;
    
    public AdvancedBatchDownloader(String saveDir) {
        this.saveDir = saveDir;
    }
    
    public AdvancedBatchDownloader setConcurrent(int concurrent) {
        this.concurrent = concurrent;
        return this;
    }
    
    public AdvancedBatchDownloader setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }
    
    public AdvancedBatchDownloader setSpeedLimit(long speedLimitKB) {
        this.speedLimit = speedLimitKB * 1024;
        return this;
    }
    
    public AdvancedBatchDownloader setResume(boolean resume) {
        this.resume = resume;
        return this;
    }
    
    public AdvancedBatchDownloader setProgressCallback(Consumer<ProgressInfo> callback) {
        this.progressCallback = callback;
        return this;
    }
    
    public AdvancedBatchDownloader setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
        return this;
    }
    
    /**
     * 批量下载
     */
    public DownloadResult download(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            log("没有下载任务");
            return new DownloadResult();
        }
        
        // 创建保存目录
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        log("开始批量下载...");
        log("总任务数: " + urls.size());
        log("并发数: " + concurrent);
        log("保存目录: " + saveDir);
        log("断点续传: " + (resume ? "启用" : "禁用"));
        log("=".repeat(50));
        
        DownloadResult result = new DownloadResult();
        result.total = urls.size();
        
        long startTime = System.currentTimeMillis();
        
        // 创建任务队列
        BlockingQueue<DownloadTaskInfo> taskQueue = new LinkedBlockingQueue<>();
        for (String url : urls) {
            String filename = getFilenameFromUrl(url);
            String filepath = saveDir + File.separator + filename;
            String temppath = filepath + ".tmp";
            taskQueue.offer(new DownloadTaskInfo(url, filepath, temppath, filename));
        }
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(concurrent);
        CountDownLatch latch = new CountDownLatch(urls.size());
        AtomicInteger completedCount = new AtomicInteger(0);
        List<Future<DownloadTaskResult>> futures = new ArrayList<>();
        
        // 提交任务
        for (int i = 0; i < concurrent; i++) {
            futures.add(executor.submit(() -> {
                DownloadTaskResult taskResult = null;
                while (!cancelled) {
                    DownloadTaskInfo task = taskQueue.poll();
                    if (task == null) break;
                    
                    taskResult = downloadWithRetry(task);
                    
                    int completed = completedCount.incrementAndGet();
                    if (taskResult.success) {
                        result.success++;
                        result.successFiles.add(taskResult.filename);
                        result.totalBytes += taskResult.fileSize;
                    } else {
                        result.failed++;
                        result.failedFiles.add(taskResult.filename);
                        result.errors.put(taskResult.filename, taskResult.error);
                    }
                    
                    // 更新进度
                    if (progressCallback != null) {
                        ProgressInfo info = new ProgressInfo();
                        info.completed = completed;
                        info.total = urls.size();
                        info.success = result.success;
                        info.failed = result.failed;
                        info.currentFile = taskResult.filename;
                        info.percent = (completed * 100) / urls.size();
                        info.totalBytes = result.totalBytes;
                        progressCallback.accept(info);
                    }
                    
                    latch.countDown();
                }
                return taskResult;
            }));
        }
        
        // 等待所有任务完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            cancelled = true;
            Thread.currentThread().interrupt();
        }
        
        // 等待所有线程结束
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        long endTime = System.currentTimeMillis();
        result.duration = endTime - startTime;
        
        log("=".repeat(50));
        log(result.toString());
        log("总耗时: " + formatDuration(result.duration));
        log("平均速度: " + formatSpeed(result.totalBytes * 1000 / result.duration));
        
        return result;
    }
    
    /**
     * 带重试的下载
     */
    private DownloadTaskResult downloadWithRetry(DownloadTaskInfo task) {
        for (int i = 1; i <= retryCount; i++) {
            if (cancelled) {
                return createFailedResult(task, "已取消");
            }
            
            DownloadTaskResult result = downloadFile(task);
            if (result.success) {
                return result;
            }
            
            log(String.format("下载失败 [%s] 重试 %d/%d: %s", 
                task.filename, i, retryCount, result.error));
            
            if (i < retryCount) {
                try {
                    Thread.sleep(1000 * i); // 递增等待时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return createFailedResult(task, "超过最大重试次数");
    }
    
    /**
     * 下载单个文件（支持断点续传）
     */
    private DownloadTaskResult downloadFile(DownloadTaskInfo task) {
        DownloadTaskResult result = new DownloadTaskResult();
        result.url = task.url;
        result.filename = task.filename;
        
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream inputStream = null;
        
        try {
            long existingSize = 0;
            File tempFile = new File(task.temppath);
            File finalFile = new File(task.filepath);
            
            // 检查已下载部分
            if (resume && tempFile.exists()) {
                existingSize = tempFile.length();
                result.downloadedSize = existingSize;
                log(String.format("断点续传 [%s]: 已下载 %.2f MB", 
                    task.filename, existingSize / 1024.0 / 1024.0));
            }
            
            URL urlObj = new URL(task.url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            // 设置断点续传
            if (resume && existingSize > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingSize + "-");
            }
            
            connection.connect();
            
            // 获取文件总大小
            long totalSize = connection.getContentLengthLong();
            if (resume && existingSize > 0 && connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                totalSize += existingSize;
            }
            result.fileSize = totalSize;
            
            inputStream = connection.getInputStream();
            raf = new RandomAccessFile(tempFile, "rw");
            raf.seek(existingSize);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long downloaded = existingSize;
            long lastLogTime = System.currentTimeMillis();
            long lastLogBytes = downloaded;
            
            while ((bytesRead = inputStream.read(buffer)) != -1 && !cancelled) {
                // 暂停检查
                while (paused && !cancelled) {
                    Thread.sleep(100);
                }
                
                // 限速
                if (speedLimit > 0) {
                    long startWrite = System.currentTimeMillis();
                    raf.write(buffer, 0, bytesRead);
                    long writeTime = System.currentTimeMillis() - startWrite;
                    long sleepTime = (bytesRead * 1000 / speedLimit) - writeTime;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } else {
                    raf.write(buffer, 0, bytesRead);
                }
                
                downloaded += bytesRead;
                
                // 定期打印进度
                long now = System.currentTimeMillis();
                if (now - lastLogTime > 2000) {
                    long speed = (downloaded - lastLogBytes) * 1000 / (now - lastLogTime);
                    log(String.format("下载中 [%s]: %.2f%% (%.2f MB/%.2f MB) %s", 
                        task.filename,
                        downloaded * 100.0 / totalSize,
                        downloaded / 1024.0 / 1024.0,
                        totalSize / 1024.0 / 1024.0,
                        formatSpeed(speed)));
                    lastLogTime = now;
                    lastLogBytes = downloaded;
                }
            }
            
            if (cancelled) {
                return createFailedResult(task, "已取消");
            }
            
            // 下载完成，重命名文件
            raf.close();
            inputStream.close();
            
            if (finalFile.exists()) {
                finalFile.delete();
            }
            tempFile.renameTo(finalFile);
            
            result.success = true;
            log(String.format("下载完成 [%s]: %.2f MB", 
                task.filename, totalSize / 1024.0 / 1024.0));
            
        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
            log(String.format("下载失败 [%s]: %s", task.filename, e.getMessage()));
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (raf != null) raf.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                // ignore
            }
        }
        
        return result;
    }
    
    /**
     * 暂停所有下载
     */
    public void pause() {
        paused = true;
        log("下载已暂停");
    }
    
    /**
     * 恢复下载
     */
    public void resume() {
        paused = false;
        log("恢复下载");
    }
    
    /**
     * 取消下载
     */
    public void cancel() {
        cancelled = true;
        log("下载已取消");
    }
    
    private void log(String message) {
        if (logCallback != null) {
            logCallback.accept(message);
        } else {
            System.out.println(message);
        }
    }
    
    private DownloadTaskResult createFailedResult(DownloadTaskInfo task, String error) {
        DownloadTaskResult result = new DownloadTaskResult();
        result.url = task.url;
        result.filename = task.filename;
        result.success = false;
        result.error = error;
        return result;
    }
    
    private String getFilenameFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            String filename = parts[parts.length - 1];
            if (filename == null || filename.isEmpty() || !filename.contains(".")) {
                filename = "file_" + System.currentTimeMillis() + ".download";
            }
            int queryIndex = filename.indexOf('?');
            if (queryIndex > 0) {
                filename = filename.substring(0, queryIndex);
            }
            return filename;
        } catch (Exception e) {
            return "file_" + System.currentTimeMillis() + ".download";
        }
    }
    
    private String formatSpeed(long bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + " B/s";
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.2f KB/s", bytesPerSecond / 1024.0);
        } else {
            return String.format("%.2f MB/s", bytesPerSecond / 1024.0 / 1024.0);
        }
    }
    
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        }
        return String.format("%d秒", seconds);
    }
    
    // 任务信息类
    static class DownloadTaskInfo {
        String url;
        String filepath;
        String temppath;
        String filename;
        
        DownloadTaskInfo(String url, String filepath, String temppath, String filename) {
            this.url = url;
            this.filepath = filepath;
            this.temppath = temppath;
            this.filename = filename;
        }
    }
    
    // 结果类
    public static class DownloadResult {
        public int total = 0;
        public int success = 0;
        public int failed = 0;
        public long totalBytes = 0;
        public long duration = 0;
        public List<String> successFiles = new ArrayList<>();
        public List<String> failedFiles = new ArrayList<>();
        public Map<String, String> errors = new HashMap<>();
        
        @Override
        public String toString() {
            return String.format("下载完成 - 总计: %d, 成功: %d, 失败: %d, 总大小: %s", 
                               total, success, failed, formatSize(totalBytes));
        }
        
        private String formatSize(long size) {
            if (size < 1024) return size + " B";
            int exp = (int) (Math.log(size) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.2f %sB", size / Math.pow(1024, exp), pre);
        }
    }
    
    static class DownloadTaskResult {
        String url;
        String filename;
        boolean success;
        long fileSize;
        long downloadedSize;
        String error;
    }
    
    public static class ProgressInfo {
        public int completed;
        public int total;
        public int success;
        public int failed;
        public String currentFile;
        public int percent;
        public long totalBytes;
        
        @Override
        public String toString() {
            return String.format("进度: %d%% (%d/%d) - 成功: %d, 失败: %d - 当前: %s", 
                               percent, completed, total, success, failed, currentFile);
        }
    }
}