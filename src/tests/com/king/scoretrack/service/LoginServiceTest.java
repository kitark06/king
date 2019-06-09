package com.king.scoretrack.service;

import com.king.scoretrack.model.SessionInfo;
import com.king.scoretrack.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.assertFalse;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoginService.class)
class LoginServiceTest
{

    private LoginService service;

    public LoginServiceTest()
    {

    }

    @Before
    public void setUp()
    {
        service = new LoginService(11, 11, 8, 10);
    }

    @Test
    public void doLogin_ReturnsSessionIdSavesUserSession_WhenInvoked()
    {
        String sessionId = service.doLogin("abc");
        Map sessionInfoMap = Whitebox.getInternalState(service, "sessionInfoMap");
        Assert.assertEquals(8, sessionId.length());
        Assert.assertEquals(1, sessionInfoMap.size());
    }

    @Test
    public void generateNewSessionId_ReturnsRandomSessionIdOfLength_WhenInvoked()
    {
        try
        {
            String id = Whitebox.invokeMethod(service, "generateNewSessionId");
            Assert.assertEquals(id.length(), 8);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void isSessionValid_ReturnsFalse_ForNullSession()
    {
        SessionInfo sessionInfo = new SessionInfo("a", Instant.now(), new User("a"));
        assertFalse(service.isSessionValid(null));
    }

    @Test
    public void isSessionValid_ReturnsFalse_ForExpiredSession()
    {
        SessionInfo sessionInfo = new SessionInfo("a", Instant.now().minusSeconds(60 * 1000), new User("a"));
        assertFalse(service.isSessionValid(sessionInfo));
    }
}
