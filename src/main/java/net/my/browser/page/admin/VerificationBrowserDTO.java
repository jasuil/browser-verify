package net.my.browser.page.admin;

import lombok.Data;


import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Locale;


public class VerificationBrowserDTO {

    public static class Input {
        @Data
        public static class VerifidationBrowser {

            private Long id;
            private Long browserAuthId;
            @NotNull
            private String userEmail;
            @NotNull
            private String browserSessionValue;
            private String ipAddress;
            private String userAgent;
            private String authCode;
            private Locale locale;
            private Integer retryCnt;
            private Integer errorCnt;
            private Date expiredAt;
        }

        @Data
        public static class BrowserAuth {
            private Long id;
            private Byte verified;
            private String idpUserName;
            private String idpUserEmail;
            private String userAgent;
            private String ipAddress;
           // private String authCode;
           // private String idpSession;
            private Integer retryCnt;
            private Integer errorCnt;
            private Date createdAt;
            private Date updatedAt;
        }

        @Data
        public static class BrowserSession {
            private Long id;
            private Long browserAuthId;
            private String browserSessionValue;
            private Date expiredAt;
            private Date createdAt;
        }

        @Data
        public static class BrowserAuthCode {
            private Long id;
            private Long browserAuthId;
            private Byte verified;
            private String authCode;
            private Date expiredAt;
            private Date createdAt;
            private Date updatedAt;
        }
    }

    public static class Output {
        @Data
        public static class VerificationBrowser {

            private Long id;
            private String browserSessionValue; //쿠키에 저장되는 인증값
            private String idpSession;//idp session cookie
            private String idpUserEmail;
            private String idpUserName;
            private String authCode;
            private String ipAdderss;
            private String userAgent; //broser agent
            private String errorMsg; //코드 오류 메시지
            private Integer errorCnt; //코드 오류 횟수
            private Integer retryCnt; //재발행 횟수
            private Date expiredAt;
            private Date createdAt;
            private Date updatedAt;
            private Byte verified;
        }

        @Data
        public static class BrowserAuth {
            private Long id;
            private Byte verified;
            private String idpUserName;
            private String idpUserEmail;
            private String userAgent;
            private String ipAddress;
            private String authCode;
            private String idpSession;
            private Integer retryCnt;
            private Integer errorCnt;
            private Date createdAt;
            private Date updatedAt;
        }

        @Data
        public static class BrowserSession {
            private Long id;
            private Long browserAuthId;
            private String browserSessionValue;
            private Date expiredAt;
            private Date createdAt;
        }

        @Data
        public static class BrowserAuthCode {
            private Long id;
            private Long browserAuthId;
            private Byte verified;
            private String authCode;
            private Date expiredAt;
            private Date createdAt;
            private Date updatedAt;
        }
    }

}
