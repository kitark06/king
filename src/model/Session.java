package model;

import java.time.Instant;

public class Session {

    final String sessionId;
    final Instant timestamp;

    public Session(String sessionId, Instant timestamp) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
