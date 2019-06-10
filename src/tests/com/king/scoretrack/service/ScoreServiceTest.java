package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.User;
import com.king.scoretrack.model.UserScore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoginService.class, ScoreService.class,ConcurrentHashMap.class})
public class ScoreServiceTest
{
    ScoreService scoreService;
    @Before
    public void setUp() throws Exception
    {
        scoreService = new ScoreService(new LoginService(11, 11, 8, 10),15);
    }

    @Test
    public void postScores_InsertsUserScore_WhenInvoked()
    {
        LoginService loginServiceMock = mock(LoginService.class);
        SessionInfo sessionInfo = new SessionInfo("EXAMPLEE", Instant.now(), new User("kartik"));
        when(loginServiceMock.getSessionInfo("EXAMPLEE")).thenReturn(sessionInfo);
        when(loginServiceMock.isSessionValid(sessionInfo)).thenReturn(true);
        Whitebox.setInternalState(scoreService, "loginService", loginServiceMock);
        scoreService.postScore("2", "EXAMPLEE",1000);

        Map<String, ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = Whitebox.getInternalState(scoreService, "userHighestScorePerLevel");
        int score = userHighestScorePerLevel.get("2").get("kartik");
        assertEquals(1000, score);

        Map<String, SortedSet<UserScore>> levelHighScoresMap = Whitebox.getInternalState(scoreService, "levelHighScoresMap");
        assertEquals(new UserScore(new User("kartik"),1000),levelHighScoresMap.get("2").first() );
    }

    @Test
    public void postScores_DoesNotInsertUserScore__WhenHigherScoreExists()
    {
        LoginService loginServiceMock = mock(LoginService.class);
        SessionInfo sessionInfo = new SessionInfo("EXAMPLEE", Instant.now(), new User("kartik"));
        Map<String, ConcurrentHashMap<String, Integer>> userHighestScorePerLevel = new ConcurrentHashMap<>();
        userHighestScorePerLevel.put("2", new ConcurrentHashMap<>());
        userHighestScorePerLevel.get("2").put("kartik",6000);
        Whitebox.setInternalState(scoreService,"userHighestScorePerLevel",userHighestScorePerLevel);
        when(loginServiceMock.getSessionInfo("EXAMPLEE")).thenReturn(sessionInfo);
        when(loginServiceMock.isSessionValid(sessionInfo)).thenReturn(true);
        Whitebox.setInternalState(scoreService, "loginService", loginServiceMock);
        when(loginServiceMock.isSessionValid(sessionInfo)).thenReturn(true);

        scoreService.postScore("2", "EXAMPLEE",1000);
        userHighestScorePerLevel = Whitebox.getInternalState(scoreService, "userHighestScorePerLevel");
        int score = userHighestScorePerLevel.get("2").get("kartik");
        assertEquals(6000, score);
    }

    @Test
    public void getHighScoreList_ReturnsSortedScores_ForLevelWithScores()
    {
        SortedSet<UserScore> userScores = Collections.synchronizedSortedSet(new TreeSet<>());
        userScores.add(new UserScore(new User("1"), 1000));
        userScores.add(new UserScore(new User("2"), 2000));
        userScores.add(new UserScore(new User("3"), 2000));
        userScores.add(new UserScore(new User("3"), 3000));

        Map<String, SortedSet<UserScore>> levelHighScoresMap = new ConcurrentHashMap<>();
        levelHighScoresMap.put("2", userScores);

        Whitebox.setInternalState(scoreService, "levelHighScoresMap", levelHighScoresMap);
        String scores = scoreService.getHighScoreList("2");
        assertEquals("3=3000,2=2000,1=1000", scores);
    }



    @Test
    public void getHighScoreList_ReturnsBlankString_ForLevelWithNoScores()
    {
        assertEquals("", scoreService.getHighScoreList(""));
    }
}