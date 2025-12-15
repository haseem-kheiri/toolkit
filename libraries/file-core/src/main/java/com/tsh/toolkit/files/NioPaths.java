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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Low-level utility methods for working with {@link Path} and {@link URI} using the NIO API.
 *
 * <p>This class provides safe conversion helpers between {@link String}, {@link URI}, and {@link
 * Path}, as well as convenience methods for resolving, normalizing, and inspecting filesystem
 * paths.
 *
 * <p>All methods are null-safe and exception-transparent where applicable. Any checked {@link
 * URISyntaxException} is wrapped in an unchecked {@link UriSyntaxException} for simplified usage in
 * internal code.
 */
public final class NioPaths {

  private NioPaths() {
    // utility class: prevent instantiation
  }

  /**
   * Converts a string representation of a URI to a {@link Path}.
   *
   * <p>Example: {@code NioPaths.toPath("file:///tmp/data.txt")}
   *
   * @param uri a string representing a URI (e.g., {@code "file:///tmp/data.txt"})
   * @return the corresponding {@link Path}
   * @throws UriSyntaxException if the given string is not a valid URI
   * @throws InvalidPathException if the URI cannot be converted to a Path
   */
  public static Path toPath(String uri) {
    try {
      return toPath(new URI(uri));
    } catch (URISyntaxException e) {
      throw new UriSyntaxException(e);
    }
  }

  /**
   * Converts a {@link URI} to a {@link Path}.
   *
   * <p>The URI must use the {@code file} scheme; other schemes are not supported.
   *
   * @param uri the URI to convert
   * @return the corresponding {@link Path}
   * @throws InvalidPathException if the URI cannot be converted to a Path
   */
  public static Path toPath(URI uri) {
    return Paths.get(uri);
  }

  /**
   * Resolves a child path against a parent path.
   *
   * @param parent the parent path
   * @param child the child segment to resolve
   * @return the resolved path
   */
  public static Path resolve(Path parent, String child) {
    return parent.resolve(child);
  }

  /**
   * Normalizes a path by removing redundant elements such as {@code .} and {@code ..}.
   *
   * @param path the path to normalize
   * @return the normalized path
   */
  public static Path normalize(Path path) {
    return path.normalize();
  }

  /**
   * Converts a {@link Path} to its corresponding {@link URI}.
   *
   * @param path the path to convert
   * @return the URI representation of the path
   */
  public static URI toUri(Path path) {
    return path.toUri();
  }

  /**
   * Checks whether a given URI string is syntactically valid and can be converted to a {@link
   * Path}.
   *
   * @param uri the URI string to check
   * @return {@code true} if valid, {@code false} otherwise
   */
  public static boolean isValidPath(String uri) {
    try {
      toPath(uri);
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }

  /**
   * Returns the file name component of a path.
   *
   * @param path the path
   * @return the file name, or {@code null} if the path has no file name
   */
  public static Path fileName(Path path) {
    return path.getFileName();
  }

  /**
   * Returns the parent directory of a given path.
   *
   * @param path the path
   * @return the parent path, or {@code null} if none exists
   */
  public static Path parent(Path path) {
    return path.getParent();
  }
}
