package com.king.scoretrack.model;

import java.util.SortedSet;
import java.util.TreeSet;

public class UserScore implements Comparable<UserScore>
{
    private String userId;
    private double score;

    public UserScore(String userId, double score)
    {
        this.userId = userId;
        this.score = score;
    }

    public UserScore(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public double getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    @Override
    public int compareTo(UserScore o)
    {
        if (this.score > o.score) return -1;
        else if (this.score < o.score) return 1;
        else return 0;
    }

    @Override
    public boolean equals(Object o)
    {
        return this.userId.equals(((UserScore) o).userId);
    }

    @Override
    public int hashCode()
    {
        return userId.hashCode();
    }

    @Override
    public String toString()
    {
        return "UserScore{" + "userId='" + userId + '\'' + ", score=" + score + '}';
    }

    public static void main(String[] args)
    {
        SortedSet<UserScore> userScores = new TreeSet<>();
        System.out.println(userScores.add(new UserScore("abc", 1000)));
        System.out.println(userScores.add(new UserScore("pqr", 2000)));
        userScores.add(new UserScore("xyz", 1000));

        userScores.forEach(userScore -> {
            System.out.println(userScore.toString());
        });
    }
}
