package es.udc.fi.dc.fd.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import es.udc.fi.dc.fd.config.WebSecurityConfigurationAware;
import org.junit.After;
import org.junit.Before;

public class UserAuthenticationIntegrationTest extends WebSecurityConfigurationAware {

	private static String SEC_CONTEXT_ATTR;

	String username;
	String pass;
	String pass2;
	
	@Before
	public void intialize(){
		username = "user@udc.es";
		SEC_CONTEXT_ATTR = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
		pass = "demo";
		pass2 = "invalid";
	}
	
	//PR-UN-034
	@Test
	public void requiresAuthentication() throws Exception {
		mockMvc.perform(get("/account/current"))
		.andExpect(redirectedUrl("http://localhost/signin"));
	}

	//PR-UN-035
	@Test
	public void userAuthenticates() throws Exception {

		mockMvc.perform(post("/authenticate").param("username", username).param("password", pass))
		.andExpect(redirectedUrl("/"))
		.andExpect(r -> Assert.assertEquals(((SecurityContext) r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR)).getAuthentication().getName(), username));

	}

	//PR-UN-036
	@Test
	public void userAuthenticationFails() throws Exception {
		mockMvc.perform(post("/authenticate").param("username", username).param("password", pass2))
		.andExpect(redirectedUrl("/signin?error=1"))
		.andExpect(r -> Assert.assertNull(r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR)));
	}
	
	@After
	public void endTest(){
		username = null;
		SEC_CONTEXT_ATTR = null;
		pass = null;
		pass2 = null;
	}
}