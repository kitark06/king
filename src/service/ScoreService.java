package service;

import model.SessionInfo;
import model.User;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreService {

    LoginService loginService;
    ConcurrentHashMap<String,Integer> userHighScoreMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, TreeMap<Integer,User>> levelHighScoresMap = new ConcurrentHashMap<>();

    public ScoreService(LoginService service) {
        this.loginService = service;
    }

    public void postScore(String levelId, String sessionId, int score) {

        // for each level
        // chmap <lvl,pq>
        // init pq to cap 15 if null
        // implement comparable
        // if score already exists for a user

        SessionInfo sessionInfo = loginService.getSessionInfo(sessionId);

        if (loginService.isSessionValid(sessionInfo))
        {
            String userId = sessionInfo.getUser().getUserId();

            //TODO test with zero .. shud not be on output

            int prevScore = userHighScoreMap.get(userId) == null ? 0 : userHighScoreMap.get(userId) ;

            // check if score is present.. if present, is current greater than prev high score
            if( score > prevScore )
            {
                // TODO sync
                // update the map with latest user highscore2
                userHighScoreMap.put(userId, score);
                // update level high score & insert into treemap
                levelHighScoresMap.get(levelId).put(score, new User(userId));
            }
        }
    }

    /*public String getHighScoreList(String level)
    {
        StringBuilder builder = new StringBuilder();
        levelHighScoresMap.get(level).keySet().forEach(score -> {
//            System.out.println(score + "="+);
        });
    }*/
}
