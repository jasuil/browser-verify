package net.my.browser;

import net.my.browser.page.admin.AES256Encrpytor;
import net.my.browser.model.tables.BrowserAuth;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
class BrowserVerifyApplicationTests {

	@Autowired
	DSLContext context;

	@Autowired
    AES256Encrpytor aes256Encrpytor;

	@Test
	@Transactional
	void contextLoads() {

		String h = context.select(net.my.browser.model.tables.Test.TEST.ID).from(net.my.browser.model.tables.Test.TEST).orderBy(net.my.browser.model.tables.Test.TEST.ID.desc()).limit(1).fetchOneInto(String.class);
		System.out.println(h);

		UpdateResultStep<Record1<Integer>> step = context.update(BrowserAuth.BROWSER_AUTH)
				.set(BrowserAuth.BROWSER_AUTH.RETRY_CNT, (context.select(BrowserAuth.BROWSER_AUTH.RETRY_CNT)
						.from(BrowserAuth.BROWSER_AUTH).where(BrowserAuth.BROWSER_AUTH.ID.eq(2L)).fetchOne(BrowserAuth.BROWSER_AUTH.RETRY_CNT)) + 1)
				.set(BrowserAuth.BROWSER_AUTH.UPDATED_AT, DSL.currentTimestamp())
				.where(BrowserAuth.BROWSER_AUTH.ID.eq(2L))

				.returningResult(BrowserAuth.BROWSER_AUTH.RETRY_CNT);

		System.out.println(step);
	}

	@Test
	public void encryptTest() throws GeneralSecurityException, UnsupportedEncodingException {
		String j = aes256Encrpytor.decrypt("Th29XGLEyN2K6Og+MQWflyFxznXKSwOPsSbtaYYn91RrYXOrdyiXJequ3ivuGEub8X+DAdx+QEUqmvcid/MjOQ==");


		Timestamp d= context.select(DSL.timestampAdd(DSL.currentTimestamp(), 90, DatePart.DAY)).from(net.my.browser.model.tables.Test.TEST).limit(1)
		.fetchOneInto(Timestamp.class);
		System.out.println(j);
	}


}
