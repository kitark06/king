import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import service.LoginService;
import service.ScoreService;
import util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HttpResponder
{

    private void start() throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        HttpContext rootContext = server.createContext("/");
        LoginService loginService = new LoginService();
        ScoreService scoreService = new ScoreService(loginService);

        rootContext.setHandler((handler) -> {
            String uri = handler.getRequestURI().getPath();
            String[] urlSplits = uri.split("/");
            String opType = urlSplits[urlSplits.length - 1];

            switch (opType)
            {
                // eg http://localhost:8081/4711/login
                case Constants.LOGIN:
                {
                    String userId = urlSplits[urlSplits.length - 2];
                    String payload = loginService.doLogin_Get(userId);
                    handler.sendResponseHeaders(200, payload.getBytes().length);
                    final OutputStream output = handler.getResponseBody();
                    output.write(payload.getBytes());
                    output.flush();
                    handler.close();
                    break;
                }

                // eg POST http://localhost:8081/2/score?sessionkey=UICSNDK (with the post body: 1500
                case Constants.SCORE:
                {
                    BufferedReader requestBodyReader = new BufferedReader(new InputStreamReader(handler.getRequestBody()));
                    String score = requestBodyReader.readLine();
                    String levelId = urlSplits[urlSplits.length - 2];
                    String sessionId = handler.getRequestURI().getQuery().split("=")[1];
                    scoreService.postScore(levelId, sessionId, Integer.parseInt(score));
                    handler.close();
                    break;
                }

                // eg http://localhost:8081/2/highscorelist
                case Constants.HIGHSCORELIST:
                {
                    String levelId = urlSplits[urlSplits.length - 2];
                    String payload = scoreService.getHighScoreList(levelId);
                    handler.sendResponseHeaders(200, payload.getBytes().length);
                    final OutputStream output = handler.getResponseBody();
                    output.write(payload.getBytes());
                    output.flush();
                    handler.close();
                    break;
                }

                default:
                    // code to be executed if all cases are not matched;
            }
        });

        server.start();
    }

    public static void main(String[] args) throws IOException
    {
        new HttpResponder().start();
    }
}
