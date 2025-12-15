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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.tsh.toolkit.file.TempDirUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioFilesTest {

  private static Path workingDirPath;

  @BeforeAll
  static void setup() {
    // Shared temp working dir for all tests
    workingDirPath = TempDirUtils.getTempDir(NioFilesTest.class.getSimpleName()).toPath();
  }

  @AfterAll
  static void cleanup() {
    TempDirUtils.deleteRecursively(workingDirPath.toFile());
  }

  // ==================== Create / Existence ====================

  @Test
  void testCreateFile() {
    // Creating file where directory exists should throw
    IOException ex = assertThrows(IOException.class, () -> NioFiles.createFile(workingDirPath));
    assertEquals(
        String.format("Path exists and is a directory: %s", workingDirPath),
        ex.getLocalizedMessage());

    Path filePath = workingDirPath.resolve("test-file.txt");
    assertFalse(NioFiles.fileExists(filePath));

    Path created = assertDoesNotThrow(() -> NioFiles.createFile(filePath));
    assertTrue(NioFiles.fileExists(filePath));
    assertFalse(NioFiles.dirExists(filePath));

    // Re-create returns same path
    assertEquals(created, assertDoesNotThrow(() -> NioFiles.createFile(filePath)));

    // Clean up
    assertDoesNotThrow(() -> NioFiles.deleteFile(filePath));
    assertFalse(NioFiles.fileExists(filePath));
  }

  @Test
  void testCreateDirectory() {
    Path dirPath = workingDirPath.resolve(UUID.randomUUID().toString());
    assertFalse(NioFiles.dirExists(dirPath));

    assertDoesNotThrow(() -> NioFiles.createDirectory(dirPath));
    assertTrue(NioFiles.dirExists(dirPath));
  }

  @Test
  void testExistenceChecks() throws Exception {
    assertFalse(NioFiles.fileExists(workingDirPath));
    assertTrue(NioFiles.dirExists(workingDirPath));

    Path filePath = workingDirPath.resolve("exists.txt");
    assertDoesNotThrow(() -> NioFiles.createFile(filePath));

    assertTrue(NioFiles.fileExists(filePath));
    assertTrue(NioFiles.isReadable(filePath));
    assertTrue(NioFiles.isWritable(filePath));

    Path nonExist = workingDirPath.resolve("missing.txt");
    assertFalse(NioFiles.fileExists(nonExist));
    assertFalse(NioFiles.isReadable(nonExist));
    assertFalse(NioFiles.isWritable(nonExist));
  }

  // ==================== Read / Write ====================

  @Test
  void testReadWriteFile() throws Exception {
    Path filePath = workingDirPath.resolve("readwrite.txt");
    String content = "Hello, world!";

    // Write file
    assertDoesNotThrow(() -> NioFiles.writeFile(filePath, content, true));

    // Overwrite protection
    IOException ex =
        assertThrows(IOException.class, () -> NioFiles.writeFile(filePath, "New content", false));
    assertTrue(ex.getMessage().contains("overwrite is false"));

    // Read file
    String read = assertDoesNotThrow(() -> NioFiles.readFile(filePath));
    assertEquals(content, read);

    NioFiles.deleteFile(filePath);
  }

  // ==================== Copy / Move ====================

  @Test
  void testCopyMoveFile() throws Exception {
    Path source = workingDirPath.resolve("source.txt");
    Path target = workingDirPath.resolve("target.txt");

    NioFiles.writeFile(source, "copy content", true);

    // Copy without overwrite
    assertDoesNotThrow(() -> NioFiles.copyFile(source, target, true));
    assertTrue(NioFiles.fileExists(target));

    // Copy with overwrite
    assertDoesNotThrow(() -> NioFiles.copyFile(source, target, true));

    // Move file
    Path moved = workingDirPath.resolve("moved.txt");
    assertDoesNotThrow(() -> NioFiles.moveFile(target, moved, true));
    assertFalse(NioFiles.fileExists(target));
    assertTrue(NioFiles.fileExists(moved));

    // Cleanup
    NioFiles.deleteFile(source);
    NioFiles.deleteFile(moved);
  }

  // ==================== Delete ====================

  @Test
  void testDeleteDir() throws Exception {
    Path dir = workingDirPath.resolve(UUID.randomUUID().toString());
    NioFiles.createDirectory(dir);
    Path file1 = NioFiles.createFile(dir.resolve("file1.txt"));
    Path file2 = NioFiles.createFile(dir.resolve("file2.txt"));

    assertTrue(NioFiles.dirExists(dir));
    assertDoesNotThrow(() -> NioFiles.deleteDir(dir));
    assertFalse(NioFiles.dirExists(dir));
    assertFalse(NioFiles.fileExists(file1));
    assertFalse(NioFiles.fileExists(file2));
  }

  // ==================== Listing ====================

  @Test
  void testListFiles() throws Exception {
    Path dir = workingDirPath.resolve("listDir");
    NioFiles.createDirectory(dir);
    NioFiles.createFile(dir.resolve("file1.txt"));
    NioFiles.createFile(dir.resolve("file2.txt"));

    Path[] files = NioFiles.listFiles(dir);
    assertEquals(2, files.length);

    Path[] recursive = NioFiles.listFilesRecursive(workingDirPath);
    assertTrue(recursive.length >= 2);

    List<Path> allFiles = NioFiles.getAllFilesRecursive(workingDirPath);
    assertTrue(allFiles.size() >= 2);

    NioFiles.deleteDir(dir);
  }
}
