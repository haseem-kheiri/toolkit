package com.platform.common.lock.provider.impl;

import com.platform.common.lock.provider.impl.InMemoryLockProviderTest.InMemoryLockProviderTestConfig;
import com.tsh.toolkit.common.lock.LockHandle;
import com.tsh.toolkit.common.lock.impl.LockHandleImpl;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {InMemoryLockProviderTestConfig.class},
    properties = {
      "com.platform.lock.provider.inmemory.cleanupIntervalDuration=1",
      "com.platform.lock.provider.inmemory.cleanupIntervalDurationUnit=SECONDS"
    })
@EnableAutoConfiguration
class InMemoryLockProviderTest {

  public static class InMemoryLockProviderTestConfig {}

  @Autowired private InMemoryLockProvider lockProvider;

  @Test
  void testGetOwnerId() {
    IllegalArgumentException e =
        Assertions.assertThrows(IllegalArgumentException.class, () -> lockProvider.getOwner(null));
    Assertions.assertEquals("Lock id is blank.", e.getLocalizedMessage());

    e = Assertions.assertThrows(IllegalArgumentException.class, () -> lockProvider.getOwner(""));
    Assertions.assertEquals("Lock id is blank.", e.getLocalizedMessage());

    Optional<String> ownerId = lockProvider.getOwner(UUID.randomUUID().toString());
    Assertions.assertTrue(ownerId.isEmpty());
  }

  @Test
  void testTryLock() {
    IllegalArgumentException e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.tryLock(null, null));
    Assertions.assertEquals("Cannot acquire lock. Lock id is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.tryLock("", null));
    Assertions.assertEquals("Cannot acquire lock. Lock id is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> lockProvider.tryLock(UUID.randomUUID().toString(), null));
    Assertions.assertEquals("Cannot acquire lock. Ttl duration is null.", e.getLocalizedMessage());

    Optional<LockHandle> handle =
        lockProvider.tryLock(UUID.randomUUID().toString(), Duration.of(1000, ChronoUnit.MILLIS));
    Assertions.assertTrue(handle.isPresent());
    Assertions.assertEquals(
        handle.get().getOwnerId(), lockProvider.getOwner(handle.get().getLockId()).get());
    handle = lockProvider.tryLock(handle.get().getLockId(), Duration.of(1000, ChronoUnit.MILLIS));
    Assertions.assertTrue(handle.isEmpty());
  }

  @Test
  void testExtendLock() throws InterruptedException {
    IllegalArgumentException e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.extendLock(null, null));
    Assertions.assertEquals("Lock handle is null.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> lockProvider.extendLock(new LockHandleImpl(null, null), null));
    Assertions.assertEquals("Lock handle's lockId is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                lockProvider.extendLock(
                    new LockHandleImpl(UUID.randomUUID().toString(), null), null));
    Assertions.assertEquals("Lock handle's ownerId is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                lockProvider.extendLock(
                    new LockHandleImpl(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
                    null));
    Assertions.assertEquals("Ttl duration is null.", e.getLocalizedMessage());

    Assertions.assertFalse(
        lockProvider.extendLock(
            new LockHandleImpl(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
            Duration.of(1, ChronoUnit.MINUTES)));

    Optional<LockHandle> handle =
        lockProvider.tryLock(UUID.randomUUID().toString(), Duration.of(20, ChronoUnit.MILLIS));
    Assertions.assertTrue(handle.isPresent());
    Assertions.assertTrue(lockProvider.isLocked(handle.get()));
    Thread.sleep(50);
    Assertions.assertFalse(lockProvider.isLocked(handle.get()));
    lockProvider.extendLock(handle.get(), Duration.of(50, ChronoUnit.MILLIS));
    Assertions.assertTrue(lockProvider.isLocked(handle.get()));
  }

  @Test
  void testIsLocked() throws InterruptedException {
    Assertions.assertFalse(lockProvider.isLocked(UUID.randomUUID().toString()));
    Optional<LockHandle> handle =
        lockProvider.tryLock(UUID.randomUUID().toString(), Duration.of(200, ChronoUnit.MILLIS));
    Assertions.assertTrue(handle.isPresent());
    Assertions.assertFalse(
        lockProvider.isLocked(
            new LockHandleImpl(handle.get().getLockId(), UUID.randomUUID().toString())));
    Assertions.assertTrue(lockProvider.isLocked(handle.get().getLockId()));
    Assertions.assertTrue(lockProvider.isLocked(handle.get()));

    Thread.sleep(300);
    Assertions.assertFalse(lockProvider.isLocked(handle.get().getLockId()));
    Assertions.assertFalse(lockProvider.isLocked(handle.get()));
  }

  @Test
  void testReleaseLock() {
    IllegalArgumentException e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.releaseLock(null));
    Assertions.assertEquals("Lock handle is null.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> lockProvider.releaseLock(new LockHandleImpl(null, null)));
    Assertions.assertEquals("Lock handle's lockId is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> lockProvider.releaseLock(new LockHandleImpl(UUID.randomUUID().toString(), null)));
    Assertions.assertEquals("Lock handle's ownerId is blank.", e.getLocalizedMessage());

    Assertions.assertFalse(
        lockProvider.releaseLock(
            new LockHandleImpl(UUID.randomUUID().toString(), UUID.randomUUID().toString())));

    Optional<LockHandle> handle =
        lockProvider.tryLock(UUID.randomUUID().toString(), Duration.of(1, ChronoUnit.HOURS));
    Assertions.assertTrue(handle.isPresent());
    Assertions.assertTrue(lockProvider.isLocked(handle.get()));
    Assertions.assertTrue(lockProvider.releaseLock(handle.get()));
    Assertions.assertFalse(lockProvider.isLocked(handle.get()));
  }

  @Test
  void testForceReleased() {
    IllegalArgumentException e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.forceRelease(null));
    Assertions.assertEquals("Lock id is blank.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> lockProvider.forceRelease(""));
    Assertions.assertEquals("Lock id is blank.", e.getLocalizedMessage());

    Assertions.assertFalse(lockProvider.forceRelease(UUID.randomUUID().toString()));

    Optional<LockHandle> handle =
        lockProvider.tryLock(UUID.randomUUID().toString(), Duration.of(1, ChronoUnit.HOURS));
    Assertions.assertTrue(handle.isPresent());
    Assertions.assertTrue(lockProvider.isLocked(handle.get()));
    Assertions.assertTrue(lockProvider.forceRelease(handle.get().getLockId()));
    Assertions.assertFalse(lockProvider.isLocked(handle.get()));
  }
}
