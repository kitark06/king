package service;

import model.SessionInfo;
import model.UserScore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class ScoreService
{
    LoginService loginService;
    ConcurrentHashMap<String, Integer> userHighScoreMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, PriorityBlockingQueue<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();

    public ScoreService(LoginService service)
    {
        this.loginService = service;
    }

    public void postScore(String levelId, String sessionId, int score)
    {
        SessionInfo sessionInfo = loginService.getSessionInfo(sessionId);

        if (loginService.isSessionValid(sessionInfo))
        {
            String userId = sessionInfo.getUser().getUserId();

            PriorityBlockingQueue<UserScore> highScorePriorityQueue = levelHighScoresMap.get(levelId);

            if(highScorePriorityQueue==null)
                highScorePriorityQueue = new PriorityBlockingQueue<>(15);

            //TODO test with zero .. shud not be on output

            int prevScore = userHighScoreMap.get(userId) == null ? -1 : userHighScoreMap.get(userId);

            // check if score is present.. if present, is current greater than prev high score
            if (score > prevScore)
            {
                // TODO sync
                // update the map with latest user highscore2
                userHighScoreMap.put(userId, score);
                // update level high score & insert into treemap
                highScorePriorityQueue.remove(new UserScore(userId, score));
                highScorePriorityQueue.add(new UserScore(userId, score));
            }
        }
    }

    public String getHighScoreList(String level)
    {
        StringBuilder builder = new StringBuilder();
        levelHighScoresMap.get(level).forEach(userScore -> {
            builder.append(userScore.getUserId() + "="+userScore.getScore())
                   .append(",");
            System.out.println(userScore.getUserId() + "="+userScore.getScore());
        });
        builder.setLength(builder.length()-1);
        return builder.toString();
    }
}
