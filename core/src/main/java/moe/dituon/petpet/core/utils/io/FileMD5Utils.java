package moe.dituon.petpet.core.utils.io;

import java.io.*;
import java.security.*;
import java.util.*;

public class FileMD5Utils {
    private FileMD5Utils() {
    }

    public static Map<String, String> getFileMd5Map(File rootDir) throws IOException {
        if (rootDir == null) {
            return Collections.emptyMap();
        }
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Input must be a directory");
        }

        Map<String, String> resultMap = new HashMap<>();
        processDirectory(rootDir, rootDir, resultMap);
        return resultMap;
    }

    private static void processDirectory(File rootDir, File currentDir, Map<String, String> map) throws IOException {
        for (File file : Objects.requireNonNull(currentDir.listFiles())) {
            if (file.isDirectory()) {
                processDirectory(rootDir, file, map);
            } else {
                String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();
                String md5 = calculateMD5(file);
                map.put(relativePath, md5);
            }
        }
    }

    public static String calculateMD5(File file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found");
        }
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] md5Bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
