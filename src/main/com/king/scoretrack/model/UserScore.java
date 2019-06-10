package com.king.scoretrack.model;

/**
 * The model object UserScore. It has a reference to the User object and the score of the user.
 * The score of the user is a double to allow multiple same scores to be entered on the treeset used for calc high scores in ScoresService.
 */
public class UserScore implements Comparable<UserScore> {

    private final User user;
    private double score;

    /**
     * Instantiates a new User score.
     *
     * @param user  the User object
     * @param score the Score
     */
    public UserScore(User user, double score) {
        this.user = user;
        this.score = score;
    }

    /**
     * Instantiates a new UserScore.
     *
     * @param user the user
     */
    public UserScore(User user) {
        this.user = user;
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets score.
     *
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets score.
     *
     * @param score the score
     */
    public void setScore(int score) {
        this.score = score;
    }

    //TODO replace to ensure compat
    @Override
    public int compareTo(UserScore o) {
        if (this.score > o.score) return -1;
        else if (this.score < o.score) return 1;
        else return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserScore that = (UserScore) o;
        return Double.compare(that.score, score) == 0 && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return "UserScore{" +
                "user=" + user +
                ", score=" + score +
                '}';
    }
}
