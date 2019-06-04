package service;

import model.Session;
import model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class LoginService {

    ConcurrentHashMap<User, Session> userSessionMap = new ConcurrentHashMap<>();

    final char A = 65;
    final char Z = 90;

    public String doLogin_Get(String[] urlSplits) {
        String userId = urlSplits[urlSplits.length - 2];
        User user = new User(userId);
        return getUserSession(user).getSessionId();
    }

    private Session getUserSession(User user) {
        Session existingSession = userSessionMap.get(user);

        // if session is not present or has expired
        if (existingSession == null
                || Duration.between(existingSession.getTimestamp(), Instant.now()).toMinutes() > 10) {
            // create session
            Instant now = Instant.now();
            Session newSession = new Session(generateSessionId(8), now);

            // set the session in the map
            userSessionMap.put(user,newSession);

            // return it
            return newSession;
        }

        return existingSession;
    }

    public String generateSessionId(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (Math.round(Math.random() * (Z - A)) + A);
            builder.append(randomChar);
        }
        return builder.toString();
    }
}
