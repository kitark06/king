package com.king.scoretrack;

import com.king.scoretrack.service.LoginService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class EndToEndHttpTest
{
    private LoginService loginService;

    @BeforeClass
    public static void setUp() throws IOException
    {
        RequestInterceptor interceptor = new RequestInterceptor();
        interceptor.loadPropertyFile("");
        interceptor.start();
    }

    @Test
    public void singleLevelInputTest() throws IOException, InterruptedException
    {
        String levelId = "2";
        loginUsersAndPostScores(getInputSetOne(levelId));
        Thread.sleep(5000);
        String expected = "1=20000,2=19000,3=18000,4=17000,5=16000,6=15000,7=14000,8=13000,9=12000,10=10000,11=9000,12=8000,13=7000,14=6000,15=5000";
        String actual = testHighScoresEndPoint(levelId);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void dualLevelConcurrentInputTest() throws IOException, InterruptedException
    {
        String levelId = "3";
        String levelIdTwo = "2";
        loginUsersAndPostScores(getInputSetTwo(levelId,levelIdTwo));
        Thread.sleep(5000);
        String expected = "1=20000,2=19000,3=18000,4=17000,5=16000,6=15000,7=14000,8=13000,9=12000,10=10000,11=9000,12=8000,13=7000,14=6000,15=5000";
        String actual = testHighScoresEndPoint(levelIdTwo);
        Assert.assertEquals(expected, actual);

        // 2 variations to handle randomness due to non-determinstic insert of 2 entries with same score
        expected = "12=8000,1=2000,2=1900,3=1800,5=1600,6=1500,7=1400,9=1200,4=1000,10=1000,11=900,13=700,14=600,15=500,16=400";
        String expectedTwo = "12=8000,1=2000,2=1900,3=1800,5=1600,6=1500,7=1400,9=1200,10=1000,4=1000,11=900,13=700,14=600,15=500,16=400";
        actual = testHighScoresEndPoint(levelId);
        Assert.assertTrue(actual.equals(expected)||actual.equals(expectedTwo));
    }

    private ArrayList<String[]> getInputSetOne(String levelId)
    {
        ArrayList<String[]> inputs = new ArrayList<>();

        inputs.add(new String[]{"1", levelId, "2000"});
        inputs.add(new String[]{"1", levelId, "200"});
        inputs.add(new String[]{"1", levelId, "20"});
        inputs.add(new String[]{"1", levelId, "2"});

        inputs.add(new String[]{"10", levelId, "10000"});
        inputs.add(new String[]{"11", levelId, "9000"});
        inputs.add(new String[]{"12", levelId, "8000"});
        inputs.add(new String[]{"13", levelId, "7000"});
        inputs.add(new String[]{"14", levelId, "6000"});
        inputs.add(new String[]{"15", levelId, "5000"});
        inputs.add(new String[]{"16", levelId, "4000"});
        inputs.add(new String[]{"17", levelId, "3000"});
        inputs.add(new String[]{"18", levelId, "2000"});
        inputs.add(new String[]{"19", levelId, "1000"});
        inputs.add(new String[]{"20", levelId, "900"});

        inputs.add(new String[]{"1", levelId, "20000"});
        inputs.add(new String[]{"2", levelId, "19000"});
        inputs.add(new String[]{"3", levelId, "18000"});
        inputs.add(new String[]{"4", levelId, "17000"});
        inputs.add(new String[]{"5", levelId, "16000"});
        inputs.add(new String[]{"6", levelId, "15000"});
        inputs.add(new String[]{"7", levelId, "14000"});
        inputs.add(new String[]{"8", levelId, "13000"});
        inputs.add(new String[]{"9", levelId, "12000"});

        return inputs;
    }

    private ArrayList<String[]> getInputSetTwo(String levelId,String levelIdTwo)
    {
        ArrayList<String[]> inputs = new ArrayList<>(getInputSetOne(levelIdTwo));

        inputs.add(new String[]{"1", levelId, "200"});
        inputs.add(new String[]{"1", levelId, "20"});
        inputs.add(new String[]{"1", levelId, "2"});
        inputs.add(new String[]{"1", levelId, "2"});

        inputs.add(new String[]{"10", levelId, "1000"});
        inputs.add(new String[]{"11", levelId, "900"});
        inputs.add(new String[]{"12", levelId, "8000"});
        inputs.add(new String[]{"13", levelId, "700"});
        inputs.add(new String[]{"14", levelId, "600"});
        inputs.add(new String[]{"15", levelId, "500"});
        inputs.add(new String[]{"16", levelId, "400"});
        inputs.add(new String[]{"17", levelId, "300"});
        inputs.add(new String[]{"18", levelId, "200"});
        inputs.add(new String[]{"19", levelId, "100"});
        inputs.add(new String[]{"20", levelId, "90"});

        inputs.add(new String[]{"1", levelId, "2000"});
        inputs.add(new String[]{"2", levelId, "1900"});
        inputs.add(new String[]{"3", levelId, "1800"});
        inputs.add(new String[]{"4", levelId, "1000"});
        inputs.add(new String[]{"5", levelId, "1600"});
        inputs.add(new String[]{"6", levelId, "1500"});
        inputs.add(new String[]{"7", levelId, "1400"});
        inputs.add(new String[]{"8", levelId, "100"});
        inputs.add(new String[]{"9", levelId, "1200"});

        return inputs;
    }

    private void loginUsersAndPostScores(ArrayList<String[]> inputs)
    {
        inputs.forEach(input -> {
            new Thread(() -> {
                try
                {
                    String sessionId = hitLoginEndpoint(input[0]);
                    postPlayerScore(input[1], sessionId, Integer.parseInt(input[2]));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }).start();
        });

    }

    private String hitLoginEndpoint(String userId) throws IOException
    {
        URL obj = new URL("http://localhost:8081/" + userId + "/login");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return in.readLine();
    }

    private void postPlayerScore(String levelId, String sessionId, Integer score) throws IOException
    {
        URL obj = new URL("http://localhost:8081/" + levelId + "/score?sessionkey=" + sessionId);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(String.valueOf(score));
        wr.flush();
        wr.close();
        con.getResponseCode();
    }

    private String testHighScoresEndPoint(String levelId) throws IOException
    {
        URL obj = new URL("http://localhost:8081/" + levelId + "/highscorelist");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return in.readLine();
    }
}
