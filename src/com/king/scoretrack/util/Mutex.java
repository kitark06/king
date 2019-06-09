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

        if (mutex == null || mutex.get() == null)
        {
            mutex = new WeakReference<Mutex>(new Mutex());
            locker.put(levelId, mutex);
        }
        return mutex.get();
    }

    public synchronized Mutex getLock(String levelId, String userId)
    {
        return getLock(levelId + Constants.MUTEX_DEMILITER + userId);
    }
}
