package com.king.scoretrack.util;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The type Mutex. This class is repsonsible for distributing Mutex objects which will be used as a lock.
 * These locks are either level specific or level+user specific. this makes sure that we achieve concurrency without affecting the integretity of data.
 * Parallelism is achieved levelWise. ie 2 threads can operate concurrently if they are working on separate levels.
 *
 * Mutexes are pointed by Weak references, so the mutex objects are GCed in the next gc-cycle once the sync block finishes.
 * Mutex objects are lazily instantiated & all threads requesting mutex for the same levelId will be given the same object to facilitate locking.
 */
public class Mutex
{
    private Map<String, WeakReference<Mutex>> locker = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<Mutex>>());

    /**
     * Gets lock which is level specific.
     *
     * @param levelId the level id
     * @return the lock
     */
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

    /**
     * Gets lock which is specific to a combination of level+user.
     *
     * @param levelId the level id
     * @param userId  the user id
     * @return the lock
     */
    public synchronized Mutex getLock(String levelId, String userId)
    {
        return getLock(levelId + Constants.MUTEX_DEMILITER + userId);
    }
}
