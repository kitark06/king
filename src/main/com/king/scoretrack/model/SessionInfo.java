package com.king.scoretrack.model;

import java.time.Instant;

/**
 * The model object SessionInfo. This class has session related info like the sessionId,
 * the creation times of the session [used to calculate the validity] and the user object to which the session belongs
 */
public class SessionInfo {

    private final String sessionId;
    private final Instant timestamp;
    private final User user;

    /**
     * Instantiates a new Session info.
     *
     * @param sessionId the randomnly generated session id
     * @param timestamp the creation time of the session.
     * @param user      the User object assiciated with the session.
     */
    public SessionInfo(String sessionId, Instant timestamp, User user) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.user = user;
    }

    /**
     * Gets session id.
     *
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the creation time of the session.
     *
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the User.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return this.sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }

    @Override
    public String toString() {
        return "SessionInfo{" + "sessionId='" + sessionId + '\'' + '}';
    }
}
