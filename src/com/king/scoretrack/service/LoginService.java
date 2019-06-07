package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginService
{

    ConcurrentHashMap<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    final char A = 65;
    final char Z = 90;

    public LoginService()
    {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleWithFixedDelay(() -> cleanup(), 11, 11, TimeUnit.MINUTES);
    }

    public String doLogin_Get(String userId)
    {
        User user = new User(userId);
        return createUserSession(user).getSessionId();
    }

    private SessionInfo createUserSession(User user)
    {
        // create session
        Instant timeStamp = Instant.now();
        String sessionId = generateNewSessionId(8);
        SessionInfo sessionInfo = new SessionInfo(sessionId, timeStamp, user);

        // set the session in the map
        sessionInfoMap.put(sessionInfo.getSessionId(), sessionInfo);

        return sessionInfo;
    }

    public String generateNewSessionId(int length)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            char randomChar = (char) (Math.round(Math.random() * (Z - A)) + A);
            builder.append(randomChar);
        }
        return builder.toString();
    }

    public boolean isSessionValid(SessionInfo sessionInfo)
    {
        // if sessionInfo is not present or has expired
        return sessionInfo != null && Duration.between(sessionInfo.getTimestamp(), Instant.now()).toMinutes() <= 10;
    }

    public SessionInfo getSessionInfo(String sessionId)
    {
        return sessionInfoMap.get(sessionId);
    }

    public void cleanup()
    {
        sessionInfoMap.values().forEach(sessionInfo -> {
            if (isSessionValid(sessionInfo) == false) sessionInfoMap.remove(sessionInfo.getSessionId());
        });

    }

}
