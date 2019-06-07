package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.UserScore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class ScoreService
{
    LoginService loginService;
    ConcurrentHashMap<String,ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, PriorityBlockingQueue<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();

    public ScoreService(LoginService service)
    {
        this.loginService = service;
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

            // TODO sync on levelId ... values with same level
            synchronized (this) {
                highScorePQ = levelHighScoresMap.get(levelId);
                if (highScorePQ == null) {
                    highScorePQ = new PriorityBlockingQueue<>(15);
                }

                playerHighScores = userHighestScorePerLevel.get(levelId);
                if (playerHighScores == null)
                    playerHighScores = new ConcurrentHashMap<>();
            }

            // TODO sync on level+userId ... values with same level & user
            int prevScore;
            synchronized (this) {
                Integer userScore = playerHighScores.get(userId);
                prevScore = userScore == null ? -1 : userScore;
            }

            // check if score is present.. if present, is current greater than prev high score
            if (score > prevScore)
            {
                playerHighScores.put(userId, score);

                highScorePQ.remove(new UserScore(userId,-1));
                highScorePQ.add(new UserScore(userId, score));

                //TODO figure this one out
                userHighestScorePerLevel.put(levelId,playerHighScores);
                levelHighScoresMap.put(levelId,highScorePQ);
            }
        }
    }

    public String getHighScoreList(String level)
    {
        StringBuilder builder = new StringBuilder();
        levelHighScoresMap.get(level).forEach(userScore ->
                builder.append(userScore.getUserId()).append("=").append(userScore.getScore()).append(","));
        builder.setLength(builder.length()-1);
        return builder.toString();
    }
}
