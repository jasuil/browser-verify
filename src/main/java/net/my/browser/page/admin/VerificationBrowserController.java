package net.my.browser.page.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/browser")
public class VerificationBrowserController {

    @Autowired
    VerificationBrowserService browserService;

    @PostMapping("/login")
    public VerificationBrowserDTO.Output.VerificationBrowser browserValidation(HttpServletRequest request,@RequestBody VerificationBrowserDTO.Input.BrowserAuthCode authCodeParam, @LoggedUser UserSessionDTO user, HttpServletResponse response) throws IOException {
        return browserService.codeValidation(request, authCodeParam, user, response);
    }

    @PostMapping("/resend_auth_code")
    public VerificationBrowserDTO.Output.VerificationBrowser resend(HttpServletRequest request, @LoggedUser UserSessionDTO user, HttpServletResponse response){
        //retrieve whether resend is possible
        return browserService.resend(request, user, response);
    }

}
