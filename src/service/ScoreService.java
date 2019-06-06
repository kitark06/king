package service;

import model.SessionInfo;
import model.User;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreService {

    LoginService loginService;
    ConcurrentHashMap<String,Integer> userHighScore = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, TreeMap<Integer,User>> highScoreList = new ConcurrentHashMap<>();

    public ScoreService(LoginService service) {
        this.loginService = service;
    }

    public void postScore(String levelId, String sessionId, int score) {

        SessionInfo sessionInfo = loginService.getSessionInfo(sessionId);

        if (loginService.isSessionValid(sessionInfo))
        {
            String userId = sessionInfo.getUser().getUserId();

            //TODO test with zero .. shud not be on output

            int prevScore = userHighScore.get(userId) == null ? 0 : userHighScore.get(userId) ;

            // check if score is present.. if present, is current greater than prev high score
            if( score > prevScore )
            {
                // TODO sync
                // update the map with latest user highscore
                userHighScore.put(userId, score);
                // update level high score & insert into treemap
                highScoreList.get(levelId).put(score, new User(userId));
            }
        }
    }
}
