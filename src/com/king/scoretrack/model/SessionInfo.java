package com.king.scoretrack.model;

import java.time.Instant;
import java.util.Objects;

public class SessionInfo
{

    private final String sessionId;
    private final Instant timestamp;
    private final User user;

    public SessionInfo(String sessionId, Instant timestamp, User user)
    {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.user = user;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public User getUser()
    {
        return user;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo sessionInfo = (SessionInfo) o;
        return Objects.equals(sessionId, sessionInfo.sessionId);
    }

    @Override
    public int hashCode()
    {
        return sessionId.hashCode();
    }

    @Override
    public String toString()
    {
        return "SessionInfo{" + "sessionId='" + sessionId + '\'' + '}';
    }
}
