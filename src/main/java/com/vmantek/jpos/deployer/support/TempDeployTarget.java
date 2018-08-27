package com.vmantek.jpos.deployer.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TempDeployTarget implements Runnable
{
    private final Path tmpDirPath;

    private TempDeployTarget(Path tmpDirPath)
    {
        this.tmpDirPath = tmpDirPath;
    }

    public static File create() throws IOException
    {
        final Path tmpDirPath = Files.createTempDirectory("jpos-Q2").toAbsolutePath();
        final File tmpDir = tmpDirPath.toFile();
        final Runnable task = new TempDeployTarget(tmpDirPath);
        Runtime.getRuntime().addShutdownHook(new Thread(task));
        return tmpDir;
    }

    @Override
    public void run()
    {
        try
        {
            Files.walkFileTree(tmpDirPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc) throws IOException
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
        catch (IOException ignored)
        {
        }
    }
}
