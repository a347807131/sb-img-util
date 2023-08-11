import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
public class ProcessTest {

    private Process process;

    @Test
    public void t1() throws IOException {
        log.info("imageProcessApiPy process start initing");
        Path appDir = Path.of("pyApi");
        Path exePath=appDir.resolve("app.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(exePath.toString(),"--port=8868");
        processBuilder.directory(appDir.toFile());
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
        if(process.isAlive()) {
            process.destroy();
            log.info("imageProcessApiPy process destoryed");
        }
    }
}
