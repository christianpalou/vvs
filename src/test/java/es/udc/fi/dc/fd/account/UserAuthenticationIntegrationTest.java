package es.udc.fi.dc.fd.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import es.udc.fi.dc.fd.config.WebSecurityConfigurationAware;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.transaction.Transactional;

@Transactional
public class UserAuthenticationIntegrationTest extends WebSecurityConfigurationAware {

  @Autowired
  AccountService accountService;

  private static String SEC_CONTEXT_ATTR = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

  @Test
  public void requiresAuthentication() throws Exception {
    mockMvc.perform(get("/account/current")).andExpect(redirectedUrl("http://localhost/signin"));
  }

  /**
   * Autenticación Test que comprueba la autenticación de usuario
   * 
   * @throws Exception
   *           excepción general
   */
  @Test
  public void userAuthenticates() throws Exception {
    final String username = "user3@udc.es";
    final String password = "demo";
    Account account = new Account(username, password, "ROLE_USER", username);
    accountService.save(account);

    mockMvc.perform(post("/authenticate").param("username", username).param("password", "demo"))
        .andExpect(redirectedUrl("/"))
        .andExpect(r -> Assert.assertEquals(
            ((SecurityContext) r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR))
                .getAuthentication().getName(),
            username));

  }

  /**
   * UserAutenticacion Test que comprueba fallo en autenticación
   * 
   * @throws Exception
   *           excepción general
   */
  @Test
  public void userAuthenticationFails() throws Exception {
    final String username = "user3@udc.es";
    mockMvc.perform(post("/authenticate").param("username", username).param("password", "invalid"))
        .andExpect(redirectedUrl("/signin?error=1")).andExpect(
            r -> Assert.assertNull(r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR)));
  }
}