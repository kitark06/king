package com.king.scoretrack.util;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class Mutex
{
    private Map<String, WeakReference<Mutex>> locker = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<Mutex>>());

    public synchronized Mutex getLock(String levelId)
    {
        WeakReference<Mutex> mutex = locker.get(levelId);
        if (mutex == null)
        {
            System.out.println("Creating mtx " + levelId);
            mutex = new WeakReference<Mutex>(new Mutex());
            locker.put(levelId, mutex);
        }
        return mutex.get();
    }

    public synchronized Mutex getLock(String levelId, String userId)
    {
        return getLock(levelId + Constants.MUTEX_DEMILITER + userId);
    }


    public static void main(String[] args) throws InterruptedException
    {
        Mutex mutex = new Mutex();
        mutex.getLock("abc");

        synchronized (mutex.getLock("abc"))
        {
            System.out.println("creating m1 " + mutex.locker.size());

            mutex.getLock("pqr");
            System.out.println("creating m2 " + mutex.locker.size());

            System.gc();
            Thread.sleep(5000);

            System.out.println(mutex.locker.get("abc").get());
            System.out.println(mutex.locker.get("pqr").get());

            System.out.println(mutex.locker.size());
        }
    }
}
