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

package com.tsh.toolkit.files;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class providing high-level, safe operations on files and directories using Java NIO.
 *
 * <p>{@code NioFiles} extends the functionality of {@link java.nio.file.Files} with convenient
 * helpers for common tasks such as recursive deletion, safe creation, copying, and listing of files
 * and directories.
 *
 * <p>All methods are static, null-safe where applicable, and use modern NIO APIs to ensure
 * portability and robustness.
 *
 * <p>Author: Haseem Kheiri
 */
public final class NioFiles {

  private NioFiles() {
    // Prevent instantiation
  }

  // ==================== File / Directory Creation ====================

  /**
   * Creates a file at the given path if it does not exist.
   *
   * @param path the path of the file to create
   * @return the path of the created or existing file
   * @throws IOException if the file cannot be created or the path exists as a directory
   */
  public static Path createFile(Path path) throws IOException {
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) {
        throw new IOException("Path exists and is a directory: " + path);
      }
      return path; // file already exists
    }
    return Files.createFile(path);
  }

  /**
   * Creates a directory at the given path, including any necessary parent directories.
   *
   * @param path the directory path to create
   * @return the path of the created or existing directory
   * @throws IOException if the directory cannot be created
   */
  public static Path createDirectory(Path path) throws IOException {
    return Files.createDirectories(path);
  }

  // ==================== Existence Checks ====================

  /**
   * Checks whether the specified path exists and is a regular file.
   *
   * @param path the path to check
   * @return true if the path exists and is a file, false otherwise
   */
  public static boolean fileExists(Path path) {
    return Files.exists(path) && Files.isRegularFile(path);
  }

  /**
   * Checks whether the specified path exists and is a directory.
   *
   * @param path the path to check
   * @return true if the path exists and is a directory, false otherwise
   */
  public static boolean dirExists(Path path) {
    return Files.exists(path) && Files.isDirectory(path);
  }

  /**
   * Checks whether the specified file path is readable.
   *
   * @param path the path to check
   * @return true if the file exists and is readable, false otherwise
   */
  public static boolean isReadable(Path path) {
    return Files.isReadable(path) && Files.isRegularFile(path);
  }

  /**
   * Checks whether the specified file path is writable.
   *
   * @param path the path to check
   * @return true if the file exists and is writable, false otherwise
   */
  public static boolean isWritable(Path path) {
    return Files.isWritable(path) && Files.isRegularFile(path);
  }

  // ==================== Delete Operations ====================

  /**
   * Deletes a file at the specified path if it exists.
   *
   * @param path the file path to delete
   * @throws IOException if deletion fails
   */
  public static void deleteFile(Path path) throws IOException {
    if (fileExists(path)) {
      Files.delete(path);
    }
  }

  /**
   * Deletes a directory and all its contents recursively.
   *
   * @param path the directory path to delete
   * @throws IOException if deletion fails
   */
  public static void deleteDir(Path path) throws IOException {
    if (!dirExists(path)) {
      return;
    }

    Files.walkFileTree(
        path,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  // ==================== File Reading / Writing ====================

  /**
   * Reads the content of a file as a String.
   *
   * @param path the file path to read
   * @return the content of the file
   * @throws IOException if an I/O error occurs
   */
  public static String readFile(Path path) throws IOException {
    return Files.readString(path);
  }

  /**
   * Writes content to a file.
   *
   * @param path the file path to write
   * @param content the content to write
   * @param overwrite if true, existing file will be overwritten; otherwise an exception is thrown
   * @return the path to the written file
   * @throws IOException if an I/O error occurs
   */
  public static Path writeFile(Path path, String content, boolean overwrite) throws IOException {
    if (!overwrite && fileExists(path)) {
      throw new IOException("File exists and overwrite is false: " + path);
    }
    return Files.writeString(path, content);
  }

  // ==================== Copy / Move ====================

  /**
   * Copies a file from source to target.
   *
   * @param source the source file path
   * @param target the target file path
   * @param overwrite if true, overwrites the target if it exists
   * @return the path to the copied file
   * @throws IOException if an I/O error occurs
   */
  public static Path copyFile(Path source, Path target, boolean overwrite) throws IOException {
    CopyOption[] options =
        overwrite ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING} : new CopyOption[0];
    return Files.copy(source, target, options);
  }

  /**
   * Moves (or renames) a file from source to target.
   *
   * @param source the source file path
   * @param target the target file path
   * @param overwrite if true, overwrites the target if it exists
   * @return the path to the moved file
   * @throws IOException if an I/O error occurs
   */
  public static Path moveFile(Path source, Path target, boolean overwrite) throws IOException {
    CopyOption[] options =
        overwrite ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING} : new CopyOption[0];
    return Files.move(source, target, options);
  }

  // ==================== Directory Listing ====================

  /**
   * Lists files (non-recursively) in a directory.
   *
   * @param dir the directory path
   * @return array of file paths; empty array if directory does not exist
   * @throws IOException if an I/O error occurs
   */
  public static Path[] listFiles(Path dir) throws IOException {
    if (!dirExists(dir)) {
      return new Path[0];
    }
    try (var stream = Files.list(dir)) {
      return stream.toArray(Path[]::new);
    }
  }

  /**
   * Lists all files in a directory recursively.
   *
   * @param dir the directory path
   * @return array of file paths; empty array if directory does not exist
   * @throws IOException if an I/O error occurs
   */
  public static Path[] listFilesRecursive(Path dir) throws IOException {
    if (!dirExists(dir)) {
      return new Path[0];
    }
    try (var stream = Files.walk(dir)) {
      return stream.filter(Files::isRegularFile).toArray(Path[]::new);
    }
  }

  // ==================== Miscellaneous ====================

  /**
   * Returns a list of all files in a directory recursively.
   *
   * @param dir the directory path
   * @return list of file paths; empty list if directory does not exist
   * @throws IOException if an I/O error occurs
   */
  public static List<Path> getAllFilesRecursive(Path dir) throws IOException {
    List<Path> files = new ArrayList<>();
    if (!dirExists(dir)) {
      return files;
    }

    Files.walkFileTree(
        dir,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            files.add(file);
            return FileVisitResult.CONTINUE;
          }
        });

    return files;
  }
}
