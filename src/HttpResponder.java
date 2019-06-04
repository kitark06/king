import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import service.LoginService;
import util.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HttpResponder {

    private void start() throws IOException {
        String payload = "duke";
        HttpServer server = HttpServer.create(new InetSocketAddress(4250), 0);

        HttpContext rootContext = server.createContext("/");
        LoginService loginService = new LoginService();

        rootContext.setHandler((he) ->
        {
            String uri = he.getRequestURI().getPath();
            String[] urlSplits = uri.split("/");
            String opType = urlSplits[urlSplits.length - 1];

            switch (opType) {
                case Constants.LOGIN: {
                    loginService.doLogin_Get(urlSplits);
                    break;
                }

                case Constants.SCORE: {
                    //TODO
                    break;
                }

                case Constants.HIGHSCORELIST: {
                    //TODO
                    System.out.println("TODO");
                    break;
                }

                default:
                    // code to be executed if all cases are not matched;
            }


            he.sendResponseHeaders(200, payload.getBytes().length);
            final OutputStream output = he.getResponseBody();
            output.write(payload.getBytes());
            output.flush();
            he.close();
        });

        server.start();
    }

    public static void main(String[] args) throws IOException {
        new HttpResponder().start();
    }


}
