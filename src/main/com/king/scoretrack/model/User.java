package com.king.scoretrack.model;

/**
 * The model object User. This class as of now just has the userId which is used to track the user,
 * but can be easily made more comprehensive by adding more fields in fture
 */
public class User {

    private String userId;

    /**
     * Instantiates a new User.
     *
     * @param userId the User name/id
     */
    public User(String userId) {
        this.userId = userId;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return this.userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}
