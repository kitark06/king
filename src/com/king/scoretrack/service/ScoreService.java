package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.UserScore;
import com.king.scoretrack.util.Constants;
import com.king.scoretrack.util.Mutex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ScoreService
{
    private Mutex mutex;
    private LoginService loginService;
    private final int priorityQueueCapacity;
    private Map<String, ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = new ConcurrentHashMap<>();
    private Map<String, SortedSet<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();

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
            SortedSet<UserScore> highScores;
            ConcurrentHashMap<String, Integer> playerHighScores;

            // sync & lock requests for same levelId.
            synchronized (mutex.getLock(levelId))
            {
                highScores = levelHighScoresMap.get(levelId);
                if (highScores == null)
                {
                    highScores = Collections.synchronizedSortedSet(new TreeSet<>());
                    levelHighScoresMap.put(levelId, highScores);
                }

                playerHighScores = userHighestScorePerLevel.get(levelId);

                if (playerHighScores == null)
                {
                    playerHighScores = new ConcurrentHashMap<>();
                    userHighestScorePerLevel.put(levelId, playerHighScores);
                }
            }

            // sync & lock requests for same level & userId.
            int prevScore;
            synchronized (mutex.getLock(levelId, userId))
            {
                Integer userScore = playerHighScores.get(userId);
                prevScore = userScore == null ? -1 : userScore;
            }

            synchronized (mutex.getLock(levelId))
            {
                // check if score is present.. if present, is current greater than prev high score
                if (score > prevScore)
                {
                    playerHighScores.put(userId, score);
                    highScores.remove(new UserScore(userId));
                    boolean operationResult = highScores.add(new UserScore(userId, score));

                    // TreeMap cant have 2 users with the same score for the same level as the key here is the numeric value of the score
                    // adding a .01 if a conflict is found makes sure they are unique & can be inserted
                    // This addition is "rounded" & reverted back when the scores are sent back.
                    // since we are holding only 15 entries, even for the worst case scenario [all 15 entries have same score], the .01 additions will not change the score.
                    while(operationResult == false)
                        operationResult = highScores.add(new UserScore(userId, score+Constants.NUDGE_FACTOR));

                    if (highScores.size() > priorityQueueCapacity)
                        highScores.remove(highScores.last());
                }
            }
        }
    }

    public String getHighScoreList(String level)
    {
        StringBuilder builder = new StringBuilder();
        SortedSet<UserScore> userScores = levelHighScoresMap.get(level);
        if (userScores==null || userScores.size() == 0)
        {
            return "";
        }
        else
        {
            userScores.forEach(userScore -> builder.append(userScore.getUserId()).append("=").append(Math.round(userScore.getScore())).append(","));
            builder.setLength(builder.length() - 1);
            return builder.toString();
        }
    }
}
