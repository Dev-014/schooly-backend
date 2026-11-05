package com.school.erp.service.file;

import com.school.erp.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ClamAvScannerService {

    @Value("${app.file-management.malware-scan.enabled:false}")
    private boolean scanEnabled;

    @Value("${app.file-management.malware-scan.host:localhost}")
    private String clamAvHost;

    @Value("${app.file-management.malware-scan.port:3310}")
    private int clamAvPort;

    @Value("${app.file-management.malware-scan.timeout-ms:5000}")
    private int connectionTimeoutMs;

    public void scanInputStream(InputStream inputStream, String fileName) {
        if (!scanEnabled) {
            log.debug("Malware scanning is disabled by configuration for {}", fileName);
            return;
        }

        log.info("Initiating ClamAV malware scan for file '{}' via {}:{}", fileName, clamAvHost, clamAvPort);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(clamAvHost, clamAvPort), connectionTimeoutMs);
            socket.setSoTimeout(connectionTimeoutMs);

            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                // Send INSTREAM command
                out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
                out.flush();

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] lengthPrefix = ByteBuffer.allocate(4).putInt(bytesRead).array();
                    out.write(lengthPrefix);
                    out.write(buffer, 0, bytesRead);
                }
                // Send zero-length chunk to indicate EOF
                out.write(new byte[]{0, 0, 0, 0});
                out.flush();

                // Read ClamAV response
                StringBuilder responseBuilder = new StringBuilder();
                int responseByte;
                while ((responseByte = in.read()) != -1 && responseByte != 0 && responseByte != '\n') {
                    responseBuilder.append((char) responseByte);
                }

                String response = responseBuilder.toString().trim();
                log.info("ClamAV response for '{}': {}", fileName, response);

                if (response.endsWith("OK")) {
                    return; // Clean
                }

                if (response.contains("FOUND")) {
                    log.error("SECURITY ALERT: Malware detected in file '{}': {}", fileName, response);
                    throw new BadRequestException("Malware detected in uploaded file: " + response);
                }

                if (response.contains("ERROR")) {
                    log.warn("ClamAV returned scan error: {}", response);
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to complete ClamAV malware scan: {}", e.getMessage());
            // In development or if daemon is down, avoid breaking normal workflows if fail-open is desired,
            // or fail closed in strict mode.
            log.warn("ClamAV service unreachable; proceeding under dev policy");
        }
    }
}

