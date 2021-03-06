package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.User;
import com.king.scoretrack.model.UserScore;
import com.king.scoretrack.util.Constants;
import com.king.scoretrack.util.Mutex;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The type Score service. This service is reponsible for processing the user requests about the scores.
 * Keeps track of high scores of users for each level. Processes the given score only if the incoming score is greater than the already present score.
 * Uses a treemap with double for score
 *
 * NOTE
 * TreeMap cant have 2 users with the same score for the same level as the key here is the numeric value of the score
 * This is mitigated by adding a .01 if a conflict is found to make sure they are unique & can be inserted
 * This addition is "rounded" & reverted back when the scores are sent back. since we are holding only 15 entries, even for the worst case scenario [all 15 entries have same score], the .01 additions will not change the score.
 */

public class ScoreService
{
    private final Mutex mutex;
    private final LoginService loginService;
    private final int priorityQueueCapacity;
    private final Map<String, ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = new ConcurrentHashMap<>();
    private final Map<String, SortedSet<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Score service.
     *
     * @param service               the loginService instance which will hold the session related info for the user.
     * @param priorityQueueCapacity the priority queue capacity
     */
    public ScoreService(LoginService service, int priorityQueueCapacity)
    {
        this.mutex = new Mutex();
        this.loginService = service;
        this.priorityQueueCapacity = priorityQueueCapacity;
    }

    /**
     * This method is called by the RequestInterceptor when a call to the SCORE end point is made.
     * It first queries the login service for a valid session and only processes requests with a valid session.
     * It then roceeds to check the player high score if he has. If incoming score is greater than player high score, it updates the high score with the new value.
     * Else it skips the request.
     *
     * @param levelId   the level id
     * @param sessionId the session id of the user associated with theh request
     * @param score     the score to be posted
     */
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
                    highScores.remove(new UserScore(new User(userId)));
                    boolean operationResult = highScores.add(new UserScore(new User(userId), score));

                    // TreeMap cant have 2 users with the same score for the same level as the key here is the numeric value of the score
                    // adding a .01 if a conflict is found makes sure they are unique & can be inserted
                    // This addition is "rounded" & reverted back when the scores are sent back.
                    // since we are holding only 15 entries, even for the worst case scenario [all 15 entries have same score], the .01 additions will not change the score.
                    while(operationResult == false)
                        operationResult = highScores.add(new UserScore(new User(userId), score+Constants.NUDGE_FACTOR));

                    if (highScores.size() > priorityQueueCapacity)
                        highScores.remove(highScores.last());
                }
            }
        }
    }

    /**
     * Gets high score list.
     * Reverts the addition of nudge factor by rounding the scores so that we can have 2 high scores with the same value in a treemap.
     * Read class level javadoc for more info.
     *
     * @param level the level
     * @return the high score list
     */
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
            userScores.forEach(userScore -> builder.append(userScore.getUser().getUserId()).append("=").append(Math.round(userScore.getScore())).append(","));
            builder.setLength(builder.length() - 1);
            return builder.toString();
        }
    }
}
