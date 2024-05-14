package com.tinusj.filewatcher.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DirectoryUtils {

    /**
     * Copies a file from the source path to the target path.
     *
     * @param sourcePath The source file path.
     * @param targetPath The target file path.
     */
    public static void copyFile(Path sourcePath, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
