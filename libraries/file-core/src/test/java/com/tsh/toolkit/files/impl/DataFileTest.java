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

package com.tsh.toolkit.files.impl;

import com.tsh.toolkit.codec.messagepack.impl.MessagePackCodec;
import com.tsh.toolkit.dataset.People;
import com.tsh.toolkit.dataset.People.Person;
import com.tsh.toolkit.files.NioFiles;
import com.tsh.toolkit.files.NioPaths;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class DataFileTest {
  private static final int GENERATED_ROWS = 1_000;
  private final MessagePackCodec serializer = new MessagePackCodec();
  private final Path path = NioPaths.toPath("file:///localmnt/contact");

  @Order(1)
  @Test
  void testWrite() throws IOException {
    NioFiles.deleteDir(path);
    try (final DataFileWriterImpl<Person> file = new DataFileWriterImpl<>(path, serializer, 400)) {
      file.append(
          IntStream.range(0, GENERATED_ROWS)
              .boxed()
              .map(n -> People.randomPerson(UUID.randomUUID()))
              .toList());
    }
  }

  @Order(2)
  @Test
  void testRead() throws IOException {
    try (final DataFileReaderImpl<Person> file =
        new DataFileReaderImpl<>(Person.class, path, serializer)) {
      Person p;
      int index = 0;
      while ((p = file.readNext()) != null) {
        index++;
        log.info("{} {}", index, p.toString());
      }
      Assertions.assertEquals(GENERATED_ROWS, index);
    } finally {
      NioFiles.deleteDir(path);
    }
  }
}
