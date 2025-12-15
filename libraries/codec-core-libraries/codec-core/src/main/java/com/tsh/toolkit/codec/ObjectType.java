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

package com.tsh.toolkit.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Captures and preserves generic type information at runtime.
 *
 * <p>Due to Java's type erasure, generic type parameters are normally not available at runtime.
 * {@code ObjectType<T>} uses the anonymous subclass pattern to retain the actual generic type,
 * allowing reflective access to {@link Type} and detection of parameterized types.
 *
 * <p>Typical usage:
 *
 * <pre>{@code
 * ObjectType<List<String>> type = new ObjectType<List<String>>() {};
 * System.out.println(type.getType());          // prints: java.util.List<java.lang.String>
 * System.out.println(type.isParameterized());  // prints: true
 * }</pre>
 *
 * <p>If {@code ObjectType} is instantiated directly without using an anonymous subclass, such as
 * {@code new ObjectType<String>()}, the type parameter information is erased and an {@link
 * IllegalStateException} will be thrown.
 *
 * @param <T> the generic type to capture
 */
public class ObjectType<T> {

  /** The captured runtime type of {@code T}. */
  private final Type type;

  /**
   * Constructs a new {@code ObjectType} and captures the actual generic type argument of {@code T}.
   *
   * <p>This constructor must be invoked using an anonymous subclass to preserve generic type
   * information. For example:
   *
   * <pre>{@code
   * ObjectType<Map<String, Integer>> type = new ObjectType<Map<String, Integer>>() {};
   * }</pre>
   *
   * @throws IllegalStateException if the type parameter information is not available (e.g., when
   *     instantiated directly without an anonymous subclass)
   */
  protected ObjectType() {
    Type superClass = getClass().getGenericSuperclass();
    if (superClass instanceof ParameterizedType) {
      this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    } else {
      throw new IllegalStateException(
          "Type parameter missing. Use anonymous subclass, e.g. new ObjectType<Foo>() {}");
    }
  }

  /**
   * Returns the captured {@link Type} of {@code T}.
   *
   * <p>This may be a {@link Class} (for non-parameterized types) or a {@link ParameterizedType}
   * (for parameterized generics such as {@code List<String>}).
   *
   * @return the captured runtime {@link Type} of {@code T}
   */
  public Type getType() {
    return type;
  }

  /**
   * Determines whether the captured type {@code T} is parameterized.
   *
   * @return {@code true} if {@code T} is a {@link ParameterizedType}, {@code false} otherwise
   */
  public boolean isParameterized() {
    return type instanceof ParameterizedType;
  }
}
