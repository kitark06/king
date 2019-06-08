package com.king.scoretrack.model;

public class UserScore implements Comparable<UserScore>
{
    private String userId;
    private int score;

    public UserScore(String userId, int score)
    {
        this.userId = userId;
        this.score = score;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public int getScore()
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

}


//    public static void main(String[] args) {
//        PriorityBlockingQueue<UserScore> queue = new PriorityBlockingQueue<>();
//        queue.add(new UserScore("a", 2));
//        queue.add(new UserScore("b", 3));
//        queue.remove(new UserScore("b", 0));
//
//        queue.forEach(userScore -> {
//            System.out.println(userScore);
//        });
//    }

   /*
        addx(new UserScore("a", 2));
        addx(new UserScore("b", 1));
        addx(new UserScore("b", 2));
        addx(new UserScore("b", 0));
        System.out.println(set.containsKey(new UserScore("a", 5)));
        addx(new UserScore("a", 3));
        addx(new UserScore("a", 1));


    public void addx(UserScore newVal)
    {
        int score = userHighScoreMap.get(newVal.userId) == null ? -1 : userHighScoreMap.get(newVal.userId);
        if (newVal.score > score)
        {
            userHighScoreMap.put(newVal.userId, newVal.score);
            queue.remove(newVal);
            queue.add(newVal);
        }
    }
}*/
