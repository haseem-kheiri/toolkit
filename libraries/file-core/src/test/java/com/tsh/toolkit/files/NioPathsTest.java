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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NioPathsTest {

  @Test
  @DisplayName("toPath(String) should convert valid URI string to Path")
  void testToPathFromStringValid() {
    Path path = NioPaths.toPath("file:///tmp/test.txt");
    assertEquals(Path.of("/tmp/test.txt"), path);
  }

  @Test
  @DisplayName("toPath(String) should throw UriSyntaxException for invalid URI")
  void testToPathFromStringInvalidUri() {
    assertThrows(UriSyntaxException.class, () -> NioPaths.toPath("::::bad-uri"));
  }

  @Test
  @DisplayName("toPath(URI) should convert file URI to Path")
  void testToPathFromUri() throws URISyntaxException {
    URI uri = new URI("file:///tmp/data.log");
    Path path = NioPaths.toPath(uri);
    assertEquals(Path.of("/tmp/data.log"), path);
  }

  @Test
  @DisplayName("resolve should append child to parent")
  void testResolve() {
    Path parent = Path.of("/tmp");
    Path resolved = NioPaths.resolve(parent, "child.txt");
    assertEquals(Path.of("/tmp/child.txt"), resolved);
  }

  @Test
  @DisplayName("normalize should simplify redundant elements")
  void testNormalize() {
    Path path = Path.of("/tmp/../var/./logs");
    Path normalized = NioPaths.normalize(path);
    assertEquals(Path.of("/var/logs"), normalized);
  }

  @Test
  @DisplayName("toUri should convert Path to URI")
  void testToUri() {
    Path path = Path.of("/tmp/file.txt");
    URI uri = NioPaths.toUri(path);
    assertEquals(path.toUri(), uri);
  }

  @Test
  @DisplayName("isValidPath should return true for valid file URI")
  void testIsValidPathTrue() {
    assertTrue(NioPaths.isValidPath("file:///tmp/ok.txt"));
  }

  @Test
  @DisplayName("isValidPath should return false for invalid URI")
  void testIsValidPathFalse() {
    assertFalse(NioPaths.isValidPath("bad://uri:://"));
  }

  @Test
  @DisplayName("fileName should return last component of path")
  void testFileName() {
    Path path = Path.of("/tmp/data.txt");
    assertEquals(Path.of("data.txt"), NioPaths.fileName(path));
  }

  @Test
  @DisplayName("fileName should return null if path has no name component")
  void testFileNameNull() {
    assertNull(NioPaths.fileName(Path.of("/")));
  }

  @Test
  @DisplayName("parent should return parent directory of path")
  void testParent() {
    Path path = Path.of("/tmp/data.txt");
    assertEquals(Path.of("/tmp"), NioPaths.parent(path));
  }

  @Test
  @DisplayName("parent should return null if no parent exists")
  void testParentNull() {
    assertNull(NioPaths.parent(Path.of("/")));
  }
}
