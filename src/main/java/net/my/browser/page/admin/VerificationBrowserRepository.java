package net.my.browser.page.admin;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static net.my.browser.model.Tables.BROWSER_AUTH_CODE;
import static net.my.browser.model.Tables.BROWSER_SESSION;
import static net.my.browser.model.tables.BrowserAuth.BROWSER_AUTH;


@Repository
public class VerificationBrowserRepository {

    @Autowired
    DSLContext context;

    public VerificationBrowserDTO.Output.BrowserSession getBrowserSession(String browserSessionValue) {
        return context.select(BROWSER_SESSION.ID,
                BROWSER_SESSION.BROWSER_SESSION_VALUE,
                BROWSER_SESSION.BROWSER_AUTH_ID)
                .from(BROWSER_SESSION)
                .where(BROWSER_SESSION.BROWSER_SESSION_VALUE.eq(browserSessionValue)
                        .and(BROWSER_SESSION.EXPIRED_AT.ge(DSL.currentTimestamp())))
                .fetchOneInto(VerificationBrowserDTO.Output.BrowserSession.class);
    }

    public VerificationBrowserDTO.Output.BrowserAuth getBrowserAuthById(Long id) {
        return context.select(BROWSER_AUTH.IDP_USER_EMAIL,
                BROWSER_AUTH.RETRY_CNT,
                BROWSER_AUTH.ERROR_CNT,
                BROWSER_AUTH.UPDATED_AT,
                BROWSER_AUTH.VERIFIED)
                .from(BROWSER_AUTH)
                .where(BROWSER_AUTH.ID.eq(id))
                .fetchOneInto(VerificationBrowserDTO.Output.BrowserAuth.class);
    }

    public VerificationBrowserDTO.Output.VerificationBrowser getBrowserAuthByEmail(String email) {
        return context.select(BROWSER_AUTH.ID,
                BROWSER_AUTH.VERIFIED,
                BROWSER_AUTH.IDP_USER_EMAIL,
                //     BROWSER_AUTH.BROWSER_SESSION_VALUE,
                //     BROWSER_AUTH.EXPIRED_AT,
                BROWSER_AUTH.UPDATED_AT,
                BROWSER_AUTH.ERROR_CNT,
                BROWSER_AUTH.RETRY_CNT)
                .from(BROWSER_AUTH)
                .where(BROWSER_AUTH.IDP_USER_EMAIL.eq(email)
                        //.and(BROWSER_AUTH.AUTH_CODE.eq(verifidationBrowser.getAuthCode()))
                        //.and(BROWSER_AUTH.EXPIRED_AT.ge(DSL.currentTimestamp()))
                        .and(BROWSER_AUTH.VERIFIED.eq((byte) 0)))
                .orderBy(BROWSER_AUTH.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(VerificationBrowserDTO.Output.VerificationBrowser.class);
    }

    public VerificationBrowserDTO.Output.BrowserAuthCode getBrowserAuthCodeByCodeAndId(VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode) {
        return context.select(BROWSER_AUTH_CODE.ID,
                BROWSER_AUTH_CODE.EXPIRED_AT,
                BROWSER_AUTH_CODE.VERIFIED)
                .from(BROWSER_AUTH_CODE)
                .where(BROWSER_AUTH_CODE.BROWSER_AUTH_ID.eq(browserAuthCode.getBrowserAuthId())
                        .and(BROWSER_AUTH_CODE.AUTH_CODE.eq(browserAuthCode.getAuthCode()))
                        .and(BROWSER_AUTH_CODE.EXPIRED_AT.ge(DSL.currentTimestamp()))
                .and(BROWSER_AUTH_CODE.VERIFIED.eq((byte) 0)))
                .orderBy(BROWSER_AUTH_CODE.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(VerificationBrowserDTO.Output.BrowserAuthCode.class);
    }
    /**
     * create new record into browser auth
     */
    public Long insertBrowserAuth(VerificationBrowserDTO.Input.BrowserAuth browserAuth) {
        return context.insertInto(BROWSER_AUTH)
                .set(BROWSER_AUTH.VERIFIED, (byte) 0)
                .set(BROWSER_AUTH.IDP_USER_NAME, browserAuth.getIdpUserName())
                .set(BROWSER_AUTH.IDP_USER_EMAIL, browserAuth.getIdpUserEmail())
             //   .set(BROWSER_AUTH.BROWSER_SESSION_VALUE, userSessionExtension.getBrowserSession())
                .set(BROWSER_AUTH.USER_AGENT, browserAuth.getUserAgent())
                .set(BROWSER_AUTH.IP_ADDRESS, browserAuth.getIpAddress())
           //     .set(BROWSER_AUTH.EXPIRED_AT, new Timestamp(userSessionExtension.getExpiredAt().getTime()))
                .set(BROWSER_AUTH.CREATED_AT, DSL.currentTimestamp())
                .returning(BROWSER_AUTH.ID)
                .fetchOne()
                .getId();
    }

    public void initializeRecordCnt(Long id) {
        context.update(BROWSER_AUTH)
                .set(BROWSER_AUTH.ERROR_CNT, 0)
                .set(BROWSER_AUTH.RETRY_CNT, 0)
                .where(BROWSER_AUTH.ID.eq(id))
                .execute();
    }

    public void increaseErrorCnt(Integer errorCnt, Long id) {
        context.update(BROWSER_AUTH)
                .set(BROWSER_AUTH.ERROR_CNT, (context.select(BROWSER_AUTH.ERROR_CNT)
                        .from(BROWSER_AUTH).where(BROWSER_AUTH.ID.eq(id)).fetchOne(BROWSER_AUTH.ERROR_CNT)) + 1)
                .set(BROWSER_AUTH.UPDATED_AT, DSL.currentTimestamp())
                .where(BROWSER_AUTH.ID.eq(id))
                .execute();
    }

    public void increaseRetryCnt(Integer retryCnt, Long id) {
        context.update(BROWSER_AUTH)
                .set(BROWSER_AUTH.RETRY_CNT,(context.select(BROWSER_AUTH.RETRY_CNT)
                        .from(BROWSER_AUTH).where(BROWSER_AUTH.ID.eq(id)).fetchOne(BROWSER_AUTH.RETRY_CNT)) + 1)
                .set(BROWSER_AUTH.UPDATED_AT, DSL.currentTimestamp())
                .where(BROWSER_AUTH.ID.eq(id))
                .execute();
    }

    public void changeVerifyBrowserAuth(Long id) {
        context.update(BROWSER_AUTH)
                .set(BROWSER_AUTH.VERIFIED, (byte) 1)
                .set(BROWSER_AUTH.UPDATED_AT, DSL.currentTimestamp())
                .where(BROWSER_AUTH.ID.eq(id))
                .execute();
    }

    public void changeVerifyBrowserAuthCode(Long id) {
        context.update(BROWSER_AUTH_CODE)
                .set(BROWSER_AUTH_CODE.VERIFIED, (byte) 1)
                .set(BROWSER_AUTH_CODE.UPDATED_AT, DSL.currentTimestamp())
                .where(BROWSER_AUTH_CODE.ID.eq(id))
                .execute();
    }


    public void insertBrowserAuthCode(VerificationBrowserDTO.Input.BrowserAuthCode browserAuthCode) {
        context.insertInto(BROWSER_AUTH_CODE)
                .set(BROWSER_AUTH_CODE.AUTH_CODE, browserAuthCode.getAuthCode())
                .set(BROWSER_AUTH_CODE.BROWSER_AUTH_ID, browserAuthCode.getBrowserAuthId())
                .set(BROWSER_AUTH_CODE.VERIFIED, (byte) 0)
                .set(BROWSER_AUTH_CODE.EXPIRED_AT, DSL.timestampAdd(DSL.currentTimestamp(), 15, DatePart.MINUTE))
                //.set(BROWSER_AUTH_CODE.EXPIRED_AT, new Timestamp(browserAuthCode.getExpiredAt().getTime() + (1000L * 60 * 15)))
                .execute();
    }

    public Date createBrowserSession(VerificationBrowserDTO.Input.BrowserSession browserSession) {
        return context.insertInto(BROWSER_SESSION)
                .set(BROWSER_SESSION.BROWSER_AUTH_ID, browserSession.getBrowserAuthId())
                .set(BROWSER_SESSION.BROWSER_SESSION_VALUE, browserSession.getBrowserSessionValue())
                .set(BROWSER_SESSION.EXPIRED_AT, DSL.timestampAdd(DSL.currentTimestamp(), 90, DatePart.DAY))
                //.set(BROWSER_SESSION.EXPIRED_AT, new Timestamp(browserSession.getExpiredAt().getTime()))
                .returning(BROWSER_SESSION.EXPIRED_AT)
                .fetchOne()
                .getExpiredAt();
    }


}
