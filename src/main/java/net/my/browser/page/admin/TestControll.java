package net.my.browser.page.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestControll {

    @Autowired
    VerificationBrowserService verificationBrowserService;

    @RequestMapping(value = "/test")
    public ResponseEntity<?> test(HttpServletRequest request, HttpServletResponse response){
        UserSessionDTO user = new UserSessionDTO();
        user.setEmail("jasuil@daum.net");
        user.setValidState(null);
        user.setIp("192.168.56.1");
        user.setName("성일짱");
        verificationBrowserService.verificationSession(request, response, user);

        return new ResponseEntity<String>("hhhh 성일짱 hhhh", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/cookie")
    public void setCookie(HttpServletRequest request, HttpServletResponse response, @LoggedUser UserSessionDTO user){

        Cookie cookie = WebUtils.getCookie(request, "g");

        cookie = new Cookie("g", request.getParameter("name") == null ? "name" : request.getParameter("name") );
        cookie.setMaxAge( Integer.parseInt(request.getParameter("t")) );

        response.addCookie(cookie);
    }
}
