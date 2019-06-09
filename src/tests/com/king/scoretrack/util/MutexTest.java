package com.king.scoretrack.util;

import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.lang.ref.WeakReference;
import java.util.Map;

import static org.junit.Assert.*;

@PrepareForTest(Mutex.class)
public class MutexTest
{
    @Test
    public void getLock_CreatesAndGivesLock_WhenLockNotCached()
    {
        Mutex mutex = new Mutex();
        Map<String, WeakReference<Mutex>> locker = Whitebox.getInternalState(mutex, "locker");
        Mutex lock = mutex.getLock("abc");
        assertEquals(1, locker.size());
    }

    @Test
    public void getLock_GivesExistingLockInstance_WhenLockIsCached()
    {
        Mutex mutex = new Mutex();
        Map<String, WeakReference<Mutex>> locker = Whitebox.getInternalState(mutex, "locker");

        Mutex lock = mutex.getLock("abc");
        assertEquals(1, locker.size());

        Mutex lockDuplidate = mutex.getLock("abc");
        assertEquals(1, locker.size());
    }

    @Test
    public void mutexLock_IsNotGCed_TillInUseBySyncBlock() throws InterruptedException
    {
        Mutex mutex = new Mutex();
        Map<String, WeakReference<Mutex>> locker = Whitebox.getInternalState(mutex, "locker");

        synchronized (mutex.getLock("abc"))
        {
            assertNotNull(locker.get("abc").get());
            System.gc();
            Thread.sleep(3000);
            assertNotNull(locker.get("abc").get());
        }
    }

    @Test
    public void mutexLock_IsGCed_WhenNotInUse() throws InterruptedException
    {
        Mutex mutex = new Mutex();
        Map<String, WeakReference<Mutex>> locker = Whitebox.getInternalState(mutex, "locker");

        mutex.getLock("pqr");

        synchronized (mutex.getLock("abc"))
        {
            assertNotNull(locker.get("abc").get());
            assertNotNull(locker.get("pqr").get());
            System.gc();
            Thread.sleep(3000);
            assertNotNull(locker.get("abc").get());
            assertNull(locker.get("pqr").get());
        }
    }

    @Test
    public void mutexLock_IsRecreatedWhenRequested_AfterGCed() throws InterruptedException
    {
        Mutex mutex = new Mutex();
        Map<String, WeakReference<Mutex>> locker = Whitebox.getInternalState(mutex, "locker");

        mutex.getLock("pqr");
        assertNotNull(locker.get("pqr").get());
        System.gc();
        Thread.sleep(3000);
        assertNull(locker.get("pqr").get());

        mutex.getLock("pqr");
        assertNotNull(locker.get("pqr").get());
    }
}