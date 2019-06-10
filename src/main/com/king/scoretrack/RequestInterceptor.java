package com.king.scoretrack;

import com.king.scoretrack.service.LoginService;
import com.king.scoretrack.service.ScoreService;
import com.king.scoretrack.util.Constants;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * The main Request interceptor class which creates a HttpServer & is repsonsible for catering to .
 */
public class RequestInterceptor
{

    private int initialDelay;
    private int delay;
    private int sessionIdLength;
    private int sessionTimeOutMins;
    private int priorityQueueCapacity;
    private LoginService loginService;

    /**
     * Start.
     *
     * @throws IOException the io exception
     */
    public void start() throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        //TODO change to cached
        server.setExecutor(Executors.newFixedThreadPool(4));
        System.out.println("Server started and listening at port " + server.getAddress().getPort());

        HttpContext rootContext = server.createContext("/");
        loginService = new LoginService(initialDelay, delay, sessionIdLength, sessionTimeOutMins);
        ScoreService scoreService = new ScoreService(loginService, priorityQueueCapacity);

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
                    String payload = loginService.doLogin(userId);
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
                    if (score == null)
                    {
                        handler.sendResponseHeaders(400, "Score cannot be null".getBytes().length);
                        final OutputStream output = handler.getResponseBody();
                        output.write("Score cannot be null".getBytes());
                        output.flush();
                        handler.close();
                    } else
                    {
                        try
                        {
                            int parsedScore = Integer.parseInt(score);
                            String levelId = urlSplits[urlSplits.length - 2];
                            String sessionId = handler.getRequestURI().getQuery().split("=")[1];
                            scoreService.postScore(levelId, sessionId, parsedScore);
                            long l = 1000;
                            handler.sendResponseHeaders(200, l);
                            handler.close();
                        } catch (NumberFormatException e)
                        {
                            handler.sendResponseHeaders(400, "Please enter a valid score".getBytes().length);
                            final OutputStream output = handler.getResponseBody();
                            output.write("Please enter a valid score".getBytes());
                            output.flush();
                            handler.close();
                        }
                    }
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
            }
        });

        server.start();
    }

    /**
     * Load property file.
     *
     * @param propFilePath the prop file path
     */
    public void loadPropertyFile(String propFilePath)
    {
        Properties props = new Properties();
        try
        {
            props.load(new FileReader(propFilePath));
        } catch (IOException e)
        {
            System.out.println("Property File not found. Using default values");
        }

        initialDelay = Integer.parseInt(props.getProperty(Constants.INITIAL_DELAY, "11"));
        delay = Integer.parseInt(props.getProperty(Constants.DELAY, "11"));
        sessionIdLength = Integer.parseInt(props.getProperty(Constants.SESSION_ID_LENGTH, "8"));
        sessionTimeOutMins = Integer.parseInt(props.getProperty(Constants.SESSION_TIME_OUT_MINS, "10"));
        priorityQueueCapacity = Integer.parseInt(props.getProperty(Constants.PRIORITY_QUEUE_CAPACITY, "15"));
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RequestInterceptor server = new RequestInterceptor();
        String propFilePath = "";
        try
        {
            propFilePath = args[0];
        } catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Property file argument missing. Using default values");
        }
        server.loadPropertyFile(propFilePath);
        server.start();
    }
}

