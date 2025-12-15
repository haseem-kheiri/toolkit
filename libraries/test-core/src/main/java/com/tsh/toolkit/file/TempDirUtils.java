/*
 * Copyright 2025 Haseem Kheiri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 */

package com.tsh.toolkit.file;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Utility methods for working with temporary files and directories in tests.
 *
 * <p>All paths are resolved under the JVM system temp directory (see {@code java.io.tmpdir}).
 */
public final class TempDirUtils {

  private TempDirUtils() {
    // prevent instantiation
  }

  /** Returns a {@link File} pointing to a file in the system temp directory. */
  public static File getTempWorkingDirFile(String fileName) {
    return new File(System.getProperty("java.io.tmpdir"), fileName);
  }

  /** Returns a {@link Path} pointing to a file in the system temp directory. */
  public static Path getTempWorkingDirPath(String fileName) {
    return getTempWorkingDirFile(fileName).toPath();
  }

  /** Returns (and creates if needed) a directory in the system temp directory. */
  public static File getTempDir(String dirName) {
    File dir = new File(System.getProperty("java.io.tmpdir"), dirName);
    dir.mkdirs();
    return dir;
  }

  /**
   * Creates a new isolated temp directory (randomized name) under system temp. Useful when tests
   * run in parallel.
   */
  public static File newIsolatedTempDir(String prefix) {
    File dir = new File(System.getProperty("java.io.tmpdir"), prefix + "-" + UUID.randomUUID());
    dir.mkdirs();
    return dir;
  }

  /**
   * Creates a new file inside a named directory under system temp. The directory is created if it
   * does not exist.
   *
   * @throws UncheckedIOException if the file cannot be created
   */
  public static File newFileInTempDir(String dirName, String fileName) {
    try {
      File dir = getTempDir(dirName);
      File file = new File(dir, fileName);
      if (!file.createNewFile()) {
        throw new IOException("File already exists: " + file);
      }
      return file;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Resolves a sub-file path inside a named temp directory. */
  public static Path resolveInTempDir(String dirName, String fileName) {
    return getTempDir(dirName).toPath().resolve(fileName);
  }

  /**
   * Deletes a file or directory recursively.
   *
   * @throws UncheckedIOException if any file or directory cannot be deleted
   */
  public static void deleteRecursively(File file) {
    if (file == null || !file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    if (!file.delete()) {
      throw new UncheckedIOException(
          new IOException("Failed to delete: " + file.getAbsolutePath()));
    }
  }

  /**
   * A helper wrapper that provides a temporary directory and ensures cleanup via
   * try-with-resources.
   */
  public static final class TempResource implements AutoCloseable {
    private final File dir;

    private TempResource(File dir) {
      this.dir = dir;
    }

    public File getDir() {
      return dir;
    }

    public Path getPath() {
      return dir.toPath();
    }

    @Override
    public void close() {
      deleteRecursively(dir);
    }
  }

  /** Creates an auto-cleanable temporary directory for use in try-with-resources. */
  public static TempResource withTempDir(String prefix) {
    return new TempResource(newIsolatedTempDir(prefix));
  }
}
