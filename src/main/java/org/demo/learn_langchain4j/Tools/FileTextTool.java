package org.demo.learn_langchain4j.Tools;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileTextTool {

    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static boolean hasAnyFile(Path folder) {
        if (!Files.isDirectory(folder)) {
            return false;
        }

        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            return false;
        }
    }

    public static List<Path> deleteRegularFilesInDirectory(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return List.of();
        }

        try (Stream<Path> pathStream = Files.list(directory)) {
            List<Path> files = pathStream
                    .filter(Files::isRegularFile)
                    .toList();

            for (Path file : files) {
                Files.deleteIfExists(file);
            }

            return List.copyOf(files);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete files in directory: " + directory, e);
        }
    }

    public static Path writeTxt(Path targetDirectory, List<String> contentList) {
        try {
            Files.createDirectories(targetDirectory);
            String fileName = FILE_NAME_FORMAT.format(LocalDateTime.now()) + ".txt";

            String content = "";
            if (contentList != null && !contentList.isEmpty()) {
                // 将列表元素用系统换行符连接成单个字符串
                content = String.join(System.lineSeparator(), contentList);
            }

            return Files.writeString(
                    targetDirectory.resolve(fileName),
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write txt file to: " + targetDirectory, e);
        }
    }
}

