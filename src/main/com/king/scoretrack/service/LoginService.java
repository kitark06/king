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

/**
 * The type Login service. This service is responsible for logging the user, generating a session and returning that.
 * This class is NOT idempotent ie, each invocation of the login will result in a new session to be provided to the User.
 * This class spawns a cleanup thread which periodically cleans up expired stale sessions so as to prevent a memory leak.
 */
public class LoginService {
    private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    //Used for generating the random session id sequence.
    private static final char A = 65; // The start character which represents A in ASCII.
    private static final char Z = 90;  // The end character which represents Z in ASCII.

    private final int sessionIdLength;
    private final int sessionTimeOutMins;

    /**
     * Instantiates a new Login service.
     *
     * @param initialDelay       the initial delay of the cleanup thread after which it is run for the first time
     * @param delay              the scheduling interval of subsequent runs of the cleanup thread
     * @param sessionIdLength    the length of the geneated session id. Defaults to 8
     * @param sessionTimeOutMins the session time out in minutes after which a session is declared to have expired.
     */
    public LoginService(int initialDelay, int delay, int sessionIdLength, int sessionTimeOutMins) {
        this.sessionIdLength = sessionIdLength;
        this.sessionTimeOutMins = sessionTimeOutMins;

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleWithFixedDelay(() -> cleanup(), initialDelay, delay, TimeUnit.MINUTES);
    }

    /**
     * This method is called by the RequestInterceptor when a call to the LOGIN end point is made.
     * It takes the userId & returns a sessionId which is valid for 10 mins.
     * This method is NOT idempotent.
     *
     * @param userId the user id
     * @return the sessionId
     */
    public String doLogin(String userId) {
        User user = new User(userId);
        return createUserSession(user).getSessionId();
    }

    /**
     * This method is takes a user object and then calls generateNewSessionId to get the sessionId
     * It then stores this info in a field Map of sessionId & SessionInfo which contains the User object
     * This method is NOT idempotent.
     *
     * @param user the user object for which session is to be generated
     * @return A random string of default length 8
     */
    private SessionInfo createUserSession(User user) {
        // create session
        Instant timeStamp = Instant.now();
        String sessionId = generateNewSessionId();
        SessionInfo sessionInfo = new SessionInfo(sessionId, timeStamp, user);

        // set the session in the map
        sessionInfoMap.put(sessionInfo.getSessionId(), sessionInfo);

        return sessionInfo;
    }

    /**
     * This method generates and returns a random sessionId.
     *
     * @return A random string of default length 8
     */
    private String generateNewSessionId() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sessionIdLength; i++) {
            char randomChar = (char) (Math.round(Math.random() * (Z - A)) + A);
            builder.append(randomChar);
        }
        return builder.toString();
    }

    /**
     * Takes a SessionInfo object as argument &  compares its creation time with current time.
     * If the difference between the times is greater than the timeout, session is declared invalid & expired.
     * @param sessionInfo the session info object which is to be checked
     * @return True if session hasnt timed Out. False otherwise.
     */
    boolean isSessionValid(SessionInfo sessionInfo) {
        // if sessionInfo is not present or has expired
        return sessionInfo != null && Duration.between(sessionInfo.getTimestamp(), Instant.now()).toMinutes() <= sessionTimeOutMins;
    }

    /**
     * Gets session info.
     *
     * @param sessionId the session id
     * @return the session info
     */
    SessionInfo getSessionInfo(String sessionId) {
        return sessionInfoMap.get(sessionId);
    }

    /**
     * Cleanup operation which runs in a separate thread started in the constructor.
     * Runs at equal intervals and iterates through all sessions, removing invalid sessions.
     *
     * @param sessionId the session id
     * @return the session info
     */
    private void cleanup() {
        sessionInfoMap.values().forEach(sessionInfo -> {
            if (isSessionValid(sessionInfo) == false) sessionInfoMap.remove(sessionInfo.getSessionId());
        });

    }

}
