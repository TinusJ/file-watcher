package com.tinusj.filewatcher.service;

import com.tinusj.filewatcher.config.AppConfig;
import com.tinusj.filewatcher.util.DirectoryUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class FileWatcherService {

    private final AppConfig appConfig;

    @Autowired
    public FileWatcherService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void startWatching() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(appConfig.getSourceDirectory());
            registerAllDirs(path, watchService);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    Path changedFilePath = (Path) event.context();
                    Path resolvedPath = ((Path) key.watchable()).resolve(changedFilePath);

                    if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                        if (Files.isDirectory(resolvedPath)) {
                            registerAllDirs(resolvedPath, watchService);
                        } else {
                            Path targetPath = Paths.get(appConfig.getTargetDirectory()).resolve(path.relativize(resolvedPath));
                            DirectoryUtils.copyFile(resolvedPath, targetPath);
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void registerAllDirs(Path start, WatchService watchService) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}