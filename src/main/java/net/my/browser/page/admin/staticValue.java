package net.my.browser.page.admin;

public class staticValue {

    public static final String BROWSER_IDENTITY = "_agp_browser_identity_";
    public static final String IDP_SESSION_COOKIE_NAME = "_agp_idp_session_";
    public static final String LOCALE_COOKIE_NAME = "_agp_locale_";
    public static final String USER_AGENT = "user-agent";

    enum errorType {
        VERIFICATION_CODE_INVALID,
        VERIFICATION_ERROR_CNT_OVER,
        VERIFICATION_RETRY_CNT_OVER
    }
}
