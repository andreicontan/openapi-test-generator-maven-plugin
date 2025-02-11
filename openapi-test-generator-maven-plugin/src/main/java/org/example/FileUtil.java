package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Writes the given content to the specified file.
     * Ensures that the parent directories exist before writing.
     *
     * @param file    The file to write to.
     * @param content The content to write.
     */
    public static void writeToFile(File file, String content) {
        if (file == null || content == null) {
            logger.error("File or content is null. Cannot proceed with writing.");
            throw new IllegalArgumentException("File and content must not be null.");
        }

        try {
            // Ensure parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                logger.error("Failed to create directories for file: {}", file.getAbsolutePath());
                throw new IOException("Could not create directories: " + parentDir.getAbsolutePath());
            }

            // Write content to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                logger.info("Successfully generated file: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed to write file: " + file.getAbsolutePath(), e);
        }
    }
}
