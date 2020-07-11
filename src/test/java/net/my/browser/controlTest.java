package net.my.browser;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.my.browser.page.admin.AES256Encrpytor;
import net.my.browser.page.admin.Profiles;
import net.my.browser.page.admin.VerificationBrowserDTO;
import net.my.browser.page.admin.staticValue;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class controlTest {

    @Autowired
    private Environment env;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AES256Encrpytor aes256Encrpytor;

    //ea7b63bd6f0843b188bceeebe20626f4jasuil@daum.net1573902256371"
    @Test
    public void apiTestTest() throws Exception {

        Cookie browserSessionValue = new Cookie(staticValue.BROWSER_IDENTITY, aes256Encrpytor.decrypt("1BtsHb0+fVvnnM8gHDX2GEUH7bQ1N+1Q6KXb4uIiDvVWTOW5gKja8PDn1LmI7+tRztaH9t1l1Je53Kr0t9x0tw=="));
        browserSessionValue.setMaxAge(60 * 60 * 24 * 90); //90 days
        browserSessionValue.setPath("/");

        boolean bExist = Arrays.asList(env.getActiveProfiles())
                .stream()
                .anyMatch(v -> v.equalsIgnoreCase(Profiles.RELEASE));
        if (bExist)
            browserSessionValue.setDomain("line.biz");
        else
            browserSessionValue.setDomain("line-beta.biz");

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/test")
                .param("testRun", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user-agent", "chrome")
                .cookie(browserSessionValue);
        //.content(newCostCenterJson);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        String str = mvcResult.getResponse().getContentAsString();
    }

    @Test
    public void apiLoginTest() throws Exception {

        Cookie browserSessionValue = new Cookie(staticValue.BROWSER_IDENTITY, "3432s");
        browserSessionValue.setMaxAge(60 * 60 * 24 * 90); //90 days
        browserSessionValue.setPath("/");

        boolean bExist = Arrays.asList(env.getActiveProfiles())
                .stream()
                .anyMatch(v -> v.equalsIgnoreCase(Profiles.RELEASE));
        if (bExist)
            browserSessionValue.setDomain("line.biz");
        else
            browserSessionValue.setDomain("line-beta.biz");

        VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode = new VerificationBrowserDTO.Input.BrowserAuthCode();
        browserAuthCode.setAuthCode(aes256Encrpytor.decrypt("tbm2s1fzVZ4NcEB5KLciHA=="));

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/api/browser/login")
                .content(objectMapper.writeValueAsString(browserAuthCode))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user-agent", "chrome")
                .cookie(browserSessionValue);
        //.content(newCostCenterJson);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        String str = mvcResult.getResponse().getContentAsString();
    }

    @Test
    public void apiResendTest() throws Exception {

        Cookie browserSessionValue = new Cookie(staticValue.BROWSER_IDENTITY, "3432s");
        browserSessionValue.setMaxAge(60 * 60 * 24 * 90); //90 days
        browserSessionValue.setPath("/");

        boolean bExist = Arrays.asList(env.getActiveProfiles())
                .stream()
                .anyMatch(v -> v.equalsIgnoreCase(Profiles.RELEASE));
        if (bExist)
            browserSessionValue.setDomain("line.biz");
        else
            browserSessionValue.setDomain("line-beta.biz");

        VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode = new VerificationBrowserDTO.Input.BrowserAuthCode();
        browserAuthCode.setAuthCode("5TG4fR5FScGBFD");

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/api/browser/resend_auth_code")
                .content(objectMapper.writeValueAsString(browserAuthCode))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user-agent", "chrome")
                .cookie(browserSessionValue);
        //.content(newCostCenterJson);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        String str = mvcResult.getResponse().getContentAsString();
    }


}
