package org.demo.learn_langchain4j.Tools;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class FileTextTool {

    private static final Path MEMORY_DIR = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "Memory");
    private static final Path HISTORY_DIR = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "History");
    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static boolean hasAnyFile(String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            return false;
        }

        Path folder = Paths.get(directoryPath);
        if (!Files.isDirectory(folder)) {
            return false;
        }

        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
             return false;
        }
    }

    public static Path writeTxt(Path targetDirectory, List<String> contentList) {
        try {
            Files.createDirectories(targetDirectory);
            String fileName = FILE_NAME_FORMAT.format(LocalDateTime.now()) + ".txt";
            Path targetFile = targetDirectory.resolve(fileName);

            String content = "";
            if (contentList != null && !contentList.isEmpty()) {
                // 将列表元素用系统换行符连接成单个字符串
                content = String.join(System.lineSeparator(), contentList);
            }

            Files.writeString(
                    targetFile,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
            return targetFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write txt file to: " + targetDirectory, e);
        }
    }
}

