package fun.gatsby.sbimgutil.context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
public class PyApiProcessManager {

    static PyApiProcessManager instance=null;

    private Process process;

    private PyApiProcessManager(){
        try {
            init();
        }catch (Exception e){
            log.error("Py api manager init error",e);
            throw new RuntimeException();
        }
    }

    public static PyApiProcessManager getInstance(){
        if(instance==null){
            instance=new PyApiProcessManager();
        }
        return instance;
    }

    void init() throws IOException {
        log.info("imageProcessApiPy process start initing, please wait 10s.");
        Path appDir = Path.of("pyApi");
        Path exePath=appDir.resolve("app.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(exePath.toString(),"--port=8868");
        processBuilder.directory(appDir.toFile());
        processBuilder.environment().put("U2NET_HOME","models");
        process = processBuilder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        String line = "";
        var ocrReady = false;
        while (!ocrReady) {
            line = reader.readLine();
            log.debug("line: " + line);
            if (line.contains("Serving")) {
                ocrReady = true;
            }
        }
        log.info("imageProcessApiPy process inited");
    }
    public void destroy(){
        if(process.isAlive()) {
            process.destroy();
            log.info("imageProcessApiPy process destoryed,now alive ? {}",process.isAlive());
        }
    }

    public static boolean loaded(){
        return instance!=null;
    }
}
