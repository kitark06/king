package service;

import model.SessionInfo;
import model.User;

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
        Instant now = Instant.now();
        SessionInfo sessionInfo = new SessionInfo(generateNewSessionId(8), now, user);

        // set the session in the map
        sessionInfoMap.put(sessionInfo.getSessionId(), sessionInfo);

        sessionInfoMap.forEachValue(1, session -> {System.out.println(session.getSessionId() + " --> " + isSessionValid(session));});

        // return it
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
        if (sessionInfo == null || Duration.between(sessionInfo.getTimestamp(), Instant.now()).toMinutes() > 10) return false;
        else return true;
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
