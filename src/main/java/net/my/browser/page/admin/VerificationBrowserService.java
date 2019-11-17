package net.my.browser.page.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

import static net.my.browser.page.admin.staticValue.*;
import static net.my.browser.page.admin.staticValue.errorType.*;

@Service
public class VerificationBrowserService {

    final static char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();

    @Autowired
    private VerificationBrowserRepository verificationBrowserRepository;
    @Autowired
    private Environment env;
    @Autowired
    AES256Encrpytor aes256Encrpytor;


    public void verificationSession(HttpServletRequest request, HttpServletResponse response, UserSessionDTO user) {
        Optional<Cookie> sessionCookie = Optional.ofNullable(WebUtils.getCookie(request, BROWSER_IDENTITY));

        boolean isExpired = false;
        boolean unverifiedUser = false;

        if(user.getValidState() == null || (user.getValidState() != null && user.getValidState() != UserDTO.Output.ValidState.IP)) {
            if (sessionCookie.isPresent()) {
                String encryptedBrowserSessionValue = null;
                try {
                    encryptedBrowserSessionValue =  aes256Encrpytor.encrypt(sessionCookie.get().getValue());
                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                    throw new RuntimeException("encrpytion exception;" + e.getMessage());
                }

                //fetch browser session
                VerificationBrowserDTO.Output.BrowserSession browserSession = verificationBrowserRepository.getBrowserSession(encryptedBrowserSessionValue);


                if (browserSession != null) {
                    VerificationBrowserDTO.Output.BrowserAuth browserAuthInfo = this.getBrowserAuthById(browserSession.getBrowserAuthId());
                    if (browserAuthInfo.getIdpUserEmail().equals(user.getEmail())) {
                        //ok
                    } else {
                    //    if (user.getValidState() == null || (user.getValidState() != null && user.getValidState() != UserDTO.Output.ValidState.IP)) {
                            unverifiedUser = true;
                     //   }
                    }
                } else {
                    unverifiedUser = true;
                }
            }

            if (!sessionCookie.isPresent() || unverifiedUser) {

                user.setValidState(UserDTO.Output.ValidState.BROWSER_REGISTER);

                //fetch browser auth what state is unverified
                VerificationBrowserDTO.Output.VerificationBrowser browserAuthInfo = verificationBrowserRepository.getBrowserAuthByEmail(user.getEmail());

                if (browserAuthInfo == null) {

                    VerificationBrowserDTO.Input.BrowserAuth inputData = new VerificationBrowserDTO.Input.BrowserAuth();

                    //inputData.setIdpSession(WebUtils.getCookie(request, IDP_SESSION_COOKIE_NAME).getValue());
                    inputData.setIdpUserEmail(user.getEmail());
                    inputData.setIdpUserName(user.getName());
                    inputData.setUserAgent(request.getHeader(USER_AGENT)); //todo
                    inputData.setIpAddress(user.getIp());

                    browserAuthInfo = new VerificationBrowserDTO.Output.VerificationBrowser();
                    browserAuthInfo.setId(verificationBrowserRepository.insertBrowserAuth(inputData));

                } else if (browserAuthInfo.getErrorCnt() >= 5 || browserAuthInfo.getRetryCnt() >= 5) {

                    Long expiredAuthCodeValue = browserAuthInfo.getUpdatedAt().getTime() + (1000L * 60 * 10);

                    if (expiredAuthCodeValue >= new Date().getTime()) { //not expired code validation
                       // if (user.getValidState() == null || (user.getValidState() != null && user.getValidState() != UserDTO.Output.ValidState.IP)) {
                            user.setValidState(UserDTO.Output.ValidState.BROWSER_REGISTER);
                       // }
                        //disabled button

                        //return register message
                        isExpired = true;
                    } else {
                        verificationBrowserRepository.initializeRecordCnt(browserAuthInfo.getId());
                        browserAuthInfo.setRetryCnt(0);
                        browserAuthInfo.setErrorCnt(0);
                    }
                }

                if(!isExpired) {
                    //publish auth code
                    String authCode = this.publishAuthCode();
                    String encryptedAuthCode = null;

                    //encryption
                    try {
                        encryptedAuthCode = aes256Encrpytor.encrypt(authCode);
                    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                        throw new RuntimeException("encrpytion exception;" + e.getMessage());
                    }

                    VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode = new VerificationBrowserDTO.Input.BrowserAuthCode();
                    browserAuthCode.setAuthCode(encryptedAuthCode);
                    browserAuthCode.setBrowserAuthId(browserAuthInfo.getId()); //todo; db schema column extend
                    browserAuthCode.setExpiredAt(new Date());
                    verificationBrowserRepository.insertBrowserAuthCode(browserAuthCode);

                    Locale currentLocale = this.getCurrentLocale(request);
                    //send email
                }
            }
        }

    }

    /*public VerificationBrowserDTO.Output.BrowserSession getBrowserSession(String browserSessionValue) {
        return verificationBrowserRepository.getBrowserSession(browserSessionValue);
    }*/

    public VerificationBrowserDTO.Output.BrowserAuth getBrowserAuthById(Long id) {
        return verificationBrowserRepository.getBrowserAuthById(id);
    }

//    public void insertBrowserAuth(UserSessionExtension userSessionExtension) {
     //   userSessionExtension.setExpiredAt(this.setDate(1000L * 60 * 15));
      //  verificationBrowserRepository.insertBrowserAuth(userSessionExtension);
//    }

    /**
     * browser code validation
     * @param request
     * @param authCodeParam
     * @param user
     * @param response
     * @return
     */
    @Transactional
    public VerificationBrowserDTO.Output.VerificationBrowser codeValidation( HttpServletRequest request, VerificationBrowserDTO.Input.BrowserAuthCode authCodeParam, UserSessionDTO user,HttpServletResponse response) {

        VerificationBrowserDTO.Output.VerificationBrowser browserAuthInfo = new VerificationBrowserDTO.Output.VerificationBrowser();

        browserAuthInfo = verificationBrowserRepository.getBrowserAuthByEmail(user.getEmail());

        if (browserAuthInfo != null && (browserAuthInfo.getErrorCnt() >= 5 || browserAuthInfo.getRetryCnt() >= 5)) {

            Long expiredAuthCodeValue = browserAuthInfo.getUpdatedAt().getTime() + (1000L * 60 * 10);

            if (expiredAuthCodeValue >= new Date().getTime()) { //not expired code validation
                //send response data to front-end server to make button disabled
                //return error count over message
                browserAuthInfo.setErrorMsg(VERIFICATION_ERROR_CNT_OVER.name());
                return browserAuthInfo;
            } else {
                verificationBrowserRepository.initializeRecordCnt(browserAuthInfo.getId());
                browserAuthInfo.setErrorCnt(0);
                browserAuthInfo.setRetryCnt(0);
            }
        }

        if (browserAuthInfo != null && browserAuthInfo.getId() != null) {

            String browserSessionValue = null;
            String encryptedbrowserSessionValue = null;
            String encryptedAuthCode = null;

            try {
                //browserSessionValue = aes256Encrpytor.encrypt(WebUtils.getCookie(request, BROWSER_IDENTITY).getValue());
                encryptedAuthCode = aes256Encrpytor.encrypt(authCodeParam.getAuthCode());
            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                throw new RuntimeException("encrpytion exception;" + e.getMessage());
            }

            VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCodeInput = new VerificationBrowserDTO.Input.BrowserAuthCode();
            browserAuthCodeInput.setBrowserAuthId(browserAuthInfo.getId());
            browserAuthCodeInput.setAuthCode(encryptedAuthCode);

            VerificationBrowserDTO.Output.BrowserAuthCode browserAuthCodeInfo = verificationBrowserRepository.getBrowserAuthCodeByCodeAndId(browserAuthCodeInput);


            if (browserAuthCodeInfo == null) { //not match
                verificationBrowserRepository.increaseErrorCnt(browserAuthInfo.getErrorCnt(), browserAuthInfo.getId());
                browserAuthInfo.setErrorMsg(VERIFICATION_CODE_INVALID.name());
                browserAuthInfo.setErrorCnt(browserAuthInfo.getErrorCnt() + 1);
            } else { //maybe success

                verificationBrowserRepository.changeVerifyBrowserAuth(browserAuthInfo.getId());
                verificationBrowserRepository.changeVerifyBrowserAuthCode(browserAuthCodeInfo.getId());

                //genarate browser session cookie
                browserSessionValue = this.publishBrowserSessionValue(browserAuthInfo.getIdpUserEmail());

                try {
                    encryptedbrowserSessionValue = aes256Encrpytor.encrypt(browserSessionValue);
                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                    throw new RuntimeException("encrpytion exception;" + e.getMessage());
                }

                VerificationBrowserDTO.Input.BrowserSession browserSessionInput = new VerificationBrowserDTO.Input.BrowserSession();
                browserSessionInput.setBrowserAuthId(browserAuthInfo.getId());
                browserSessionInput.setBrowserSessionValue(encryptedbrowserSessionValue);

                browserAuthInfo.setExpiredAt(verificationBrowserRepository.createBrowserSession(browserSessionInput));
                browserAuthInfo.setBrowserSessionValue(browserSessionValue);

                this.setCookie(response, browserSessionValue);

                VerificationBrowserDTO.Input.VerifidationBrowser verifidationBrowserInput = new VerificationBrowserDTO.Input.VerifidationBrowser();
                verifidationBrowserInput.setAuthCode(authCodeParam.getAuthCode());
                verifidationBrowserInput.setBrowserSessionValue(browserSessionValue);
            }

        } else {
            browserAuthInfo = new VerificationBrowserDTO.Output.VerificationBrowser();
            browserAuthInfo.setErrorMsg(VERIFICATION_CODE_INVALID.name());
        }

        return browserAuthInfo;
    }

    /**
     * verification code resend process
     * @param request
     * @param user
     * @param response
     * @return
     */
    @Transactional
    public VerificationBrowserDTO.Output.VerificationBrowser resend( HttpServletRequest request, UserSessionDTO user,HttpServletResponse response) {

        VerificationBrowserDTO.Output.VerificationBrowser browserAuthInfo = new VerificationBrowserDTO.Output.VerificationBrowser();

        browserAuthInfo = verificationBrowserRepository.getBrowserAuthByEmail(user.getEmail());

        if(browserAuthInfo != null) {
            if (browserAuthInfo.getRetryCnt() >= 5) {

                Long expiredAuthCodeValue = browserAuthInfo.getUpdatedAt().getTime() + (1000L * 60 * 10);

                if (expiredAuthCodeValue >= new Date().getTime()) { //not expired code validation
                    // if (user.getValidState() == null || (user.getValidState() != null && user.getValidState() != UserDTO.Output.ValidState.IP)) {
                    //user.setValidState(UserDTO.Output.ValidState.BROWSER_REGISTER);
                    // }
                    //disabled button

                    //return message
                    browserAuthInfo.setErrorMsg(VERIFICATION_RETRY_CNT_OVER.name());
                    return browserAuthInfo;
                } else {
                    verificationBrowserRepository.initializeRecordCnt(browserAuthInfo.getId());
                    browserAuthInfo.setRetryCnt(0);
                    browserAuthInfo.setErrorCnt(0);
                }
            }

            //republish auth code
            String authCode = this.publishAuthCode();
            String encryptedAuthCode = null;

            try {
                encryptedAuthCode = aes256Encrpytor.encrypt(authCode);
            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                throw new RuntimeException("encrpytion exception;" + e.getMessage());
            }

            VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode = new VerificationBrowserDTO.Input.BrowserAuthCode();
            browserAuthCode.setAuthCode(encryptedAuthCode);
            browserAuthCode.setBrowserAuthId(browserAuthInfo.getId());
            verificationBrowserRepository.insertBrowserAuthCode(browserAuthCode);

            verificationBrowserRepository.increaseRetryCnt(browserAuthInfo.getRetryCnt(), browserAuthInfo.getId());
            browserAuthInfo.setRetryCnt(browserAuthInfo.getRetryCnt() + 1);

            Locale currentLocale = this.getCurrentLocale(request);
            //send email

        } else { //no data
            browserAuthInfo.setErrorMsg(VERIFICATION_RETRY_CNT_OVER.name());
        }

        return browserAuthInfo;
    }

    public String publishAuthCode() {

        Random r = new Random(System.currentTimeMillis());
        char[] id = new char[14];

        for (int i = 0;  i < 14;  i++) {
            id[i] = chars[r.nextInt(chars.length)];
        }
        return String.valueOf(id);
    }

    public Locale getCurrentLocale(HttpServletRequest request) {
        Cookie localeCookie = WebUtils.getCookie(request, LOCALE_COOKIE_NAME);
        if(localeCookie == null) {
            Locale locale = request.getLocale();
            return AgpLocaleUtils.getAGPLocaleWithLanguage(locale.getLanguage());
        } else {
            return AgpLocaleUtils.getAGPLocale(localeCookie.getValue());
        }
    }

    public Date setDate(long addTime) {
        Date today = new Date();
        long browserExpiredValue = today.getTime() + addTime;
        return new Date(browserExpiredValue);
    }

    public String publishBrowserSessionValue(String userEmail) {
        UUID uuid = UUID.randomUUID();
        String browserSession = uuid.toString()
                .replaceAll("-", "")
                .concat(userEmail)
                .concat(Long.toString(new Date().getTime()));

        return browserSession;
    }

    public void setCookie(HttpServletResponse response, String value){
        Cookie cookie = new Cookie(BROWSER_IDENTITY, value);
        cookie.setMaxAge(60 * 60 * 24 * 90); //90 days
        cookie.setPath("/");

        boolean bExist = Arrays.asList(env.getActiveProfiles())
                .stream()
                .anyMatch(v -> v.equalsIgnoreCase(Profiles.RELEASE));
        if (bExist)
            cookie.setDomain("line.biz");
        else
            cookie.setDomain("line-beta.biz");

        response.addCookie(cookie);
    }
}
