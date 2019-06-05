package service;

import model.SessionInfo;
import model.User;

public class ScoreService {

    LoginService loginService;

    public ScoreService(LoginService service) {
        this.loginService = service;
    }

    public void postScore(String levelId, String sessionId, int score) {

        SessionInfo sessionInfo = loginService.getSessionInfo(sessionId);
        if (loginService.isSessionValid(sessionInfo)) {
            // TODO
            String userId = sessionInfo.getUser().getUserId();
            System.out.println(userId+" --> " + score);
            // do the operation
        }
    }
}
