package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.UserScore;
import com.king.scoretrack.util.Mutex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class ScoreService
{
    private Mutex mutex;
    private LoginService loginService;
    private final int priorityQueueCapacity;
    private Map<String, ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = new ConcurrentHashMap<>();
    private Map<String, PriorityBlockingQueue<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();

    public ScoreService(LoginService service, int priorityQueueCapacity)
    {
        this.mutex = new Mutex();
        this.loginService = service;
        this.priorityQueueCapacity = priorityQueueCapacity;
    }

    public void postScore(String levelId, String sessionId, int score)
    {
        SessionInfo sessionInfo = loginService.getSessionInfo(sessionId);

        //TODO test with zero .. shud not be on output
        if (loginService.isSessionValid(sessionInfo))
        {
            String userId = sessionInfo.getUser().getUserId();

            PriorityBlockingQueue<UserScore> highScorePQ;
            ConcurrentHashMap<String, Integer> playerHighScores;

            // sync & lock requests for same levelId.
            synchronized (mutex.getLock(levelId))
            {
                highScorePQ = levelHighScoresMap.get(levelId);
                if (highScorePQ == null)
                {
                    highScorePQ = new PriorityBlockingQueue<>(priorityQueueCapacity);
                }

                playerHighScores = userHighestScorePerLevel.get(levelId);
                if (playerHighScores == null) playerHighScores = new ConcurrentHashMap<>();
            }

            // sync & lock requests for same level & userId.
            int prevScore;
            synchronized (mutex.getLock(levelId, userId))
            {
                Integer userScore = playerHighScores.get(userId);
                prevScore = userScore == null ? -1 : userScore;
            }

            // check if score is present.. if present, is current greater than prev high score
            if (score > prevScore)
            {
                playerHighScores.put(userId, score);

                highScorePQ.remove(new UserScore(userId, -1));
                highScorePQ.add(new UserScore(userId, score));

//                //TODO figure this one out
//                if (highScorePQ.size() > priorityQueueCapacity)
//                {
//                    System.out.println("************************************");
//                    highScorePQ.remove();
//                }

                //TODO figure this one out
                userHighestScorePerLevel.put(levelId, playerHighScores);
                levelHighScoresMap.put(levelId, highScorePQ);
            }
        }
    }

    public String getHighScoreList(String level)
    {
        StringBuilder builder = new StringBuilder();
        PriorityBlockingQueue<UserScore> userScores = levelHighScoresMap.get(level);
        for (int i = 0; i < userScores.size(); i++)
        {
            UserScore userScore = userScores.peek();
            builder.append(userScore.getUserId()).append("=").append(userScore.getScore()).append(",");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }
}
