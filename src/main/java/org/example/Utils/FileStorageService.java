package org.example.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Simple utility class for file storage operations
 */
public class FileStorageService {

    // Base directory for document storage
    private static final String DOCUMENTS_DIR = "documents/";

    /**
     * Store a file in the specified subdirectory
     * @param file The file to store
     * @param subDir The subdirectory (e.g., "reparations")
     * @return The path to the stored file
     */
    public String storeFile(File file, String subDir) throws IOException {
        // Create full directory path
        String dirPath = DOCUMENTS_DIR + subDir + "/";
        Path directory = Paths.get(dirPath);

        // Create directories if they don't exist
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Generate unique filename to avoid collisions
        String uniqueFileName = System.currentTimeMillis() + "_" + file.getName();
        Path destination = Paths.get(dirPath, uniqueFileName);

        // Copy the file
        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Return the path as a string
        return destination.toString();
    }

    /**
     * Delete a file
     * @param filePath The path to the file to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
