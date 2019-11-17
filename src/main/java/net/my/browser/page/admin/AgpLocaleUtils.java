package net.my.browser.page.admin;

import java.util.Locale;

public class AgpLocaleUtils {

    public static Locale getAGPLocale(String country){
        Locale locale = null;
        switch (country){
            case "ja":
                locale = Locale.JAPAN;
            default:
                locale = Locale.KOREA;
        }
        return locale;
    }

    public static Locale getAGPLocaleWithLanguage(String lang){
        Locale locale = null;
        switch (lang){
            case "ja":
                locale = Locale.JAPAN;
            default:
                locale = Locale.KOREA;
        }
        return locale;
    }

}
