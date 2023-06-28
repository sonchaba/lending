package com.sonchaba.lending.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.paths.Paths;

import java.io.*;
import java.util.Properties;

@Service
public class DumpService {
    @Value("${sftp.host}")
    private static String SFTP_HOST;
    @Value("${sftp.port}")
    private static int SFTP_PORT;
    @Value("${sftp.username}")
    private static String SFTP_USERNAME;
    @Value("${sftp.password}")
    private static String SFTP_PASSWORD;
    @Value("${sftp.path}")
    private static String SFTP_DIRECTORY;
    private static final String DUMP_FILENAME = "backup.dump";
    @Value("${backup.database.name}")
    private static String DATABASE_NAME;

    public void createAndUploadDump() {

        byte[] dumpBytes = getDumpAsByteArray();

        uploadDumpToSFTP(dumpBytes);
    }

    private byte[] getDumpAsByteArray() {
        String input = Paths.ROOT;
        input = input.concat("/").concat(DATABASE_NAME).concat("backup");
        ProcessBuilder pb = new ProcessBuilder("C:\\\"Program Files\"\\PostgreSQL\\13\\bin\\pg_dump.exe ",
                "--host", "localhost",
                "--port", "5432",
                "--username", "postgres",
                "--no-password",
                "--format", "tar",
                "--blobs",
                "--verbose", "--file", input, DATABASE_NAME);
        try {
            Process p = pb.start();
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));
            String line = r.readLine();
            while (line != null) {
                System.err.println(line);
                line = r.readLine();
            }
            r.close();
            p.waitFor();
            return p.getInputStream().readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uploadDumpToSFTP(byte[] dumpBytes) {
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(SFTP_USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(SFTP_DIRECTORY);

            // Create an output stream from the dump bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(dumpBytes);
            InputStream inputStream = new ByteArrayInputStream(dumpBytes);

            // Upload the dump to the SFTP server
            channelSftp.put(inputStream, DUMP_FILENAME);

            channelSftp.disconnect();
            session.disconnect();
        } catch (Exception e) {
            // Handle any exceptions
        }
    }
}
