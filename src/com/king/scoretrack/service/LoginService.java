package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginService
{

    private Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private final char A = 65;
    private final char Z = 90;

    private final int sessionIdLength;
    private final int sessionTimeOutMins;

    public LoginService(int initialDelay, int delay, int sessionIdLength, int sessionTimeOutMins)
    {
        this.sessionIdLength = sessionIdLength;
        this.sessionTimeOutMins = sessionTimeOutMins;

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleWithFixedDelay(() -> cleanup(), initialDelay, delay, TimeUnit.MINUTES);
        debugger();
    }

    //TODO Remove this
    private void debugger()
    {
        sessionInfoMap.put("AAAAAAAA", new SessionInfo("AAAAAAAA",Instant.now(),new User("1")));
        sessionInfoMap.put("BBBBBBBB", new SessionInfo("BBBBBBBB",Instant.now(),new User("2")));
        sessionInfoMap.put("CCCCCCCC", new SessionInfo("CCCCCCCC",Instant.now(),new User("3")));
        sessionInfoMap.put("DDDDDDDD", new SessionInfo("DDDDDDDD",Instant.now(),new User("4")));
        sessionInfoMap.put("EEEEEEEE", new SessionInfo("EEEEEEEE",Instant.now(),new User("5")));
    }

    public String doLogin(String userId)
    {
        User user = new User(userId);
        return createUserSession(user).getSessionId();
    }

    private SessionInfo createUserSession(User user)
    {
        // create session
        Instant timeStamp = Instant.now();
        String sessionId = generateNewSessionId();
        SessionInfo sessionInfo = new SessionInfo(sessionId, timeStamp, user);

        // set the session in the map
        sessionInfoMap.put(sessionInfo.getSessionId(), sessionInfo);

        return sessionInfo;
    }

    private String generateNewSessionId()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sessionIdLength; i++)
        {
            char randomChar = (char) (Math.round(Math.random() * (Z - A)) + A);
            builder.append(randomChar);
        }
        return builder.toString();
    }

    boolean isSessionValid(SessionInfo sessionInfo)
    {
        // if sessionInfo is not present or has expired
        return sessionInfo != null && Duration.between(sessionInfo.getTimestamp(), Instant.now()).toMinutes() <= sessionTimeOutMins;
    }

    SessionInfo getSessionInfo(String sessionId)
    {
        return sessionInfoMap.get(sessionId);
    }

    private void cleanup()
    {
        sessionInfoMap.values().forEach(sessionInfo -> {
            if (isSessionValid(sessionInfo) == false) sessionInfoMap.remove(sessionInfo.getSessionId());
        });

    }

}
