package es.udc.fi.dc.fd.account;

import static java.util.function.Predicate.isEqual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import es.udc.fi.dc.fd.config.ApplicationConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@WebAppConfiguration
@Transactional
public class AccountServiceTest {

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountRepository accountRepository;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  Account account;
  Account account2;
  Account account3;
  Account account4;
  Account account5;

  /**
   * 
   * @throws SaveNotAvailableException
   */
  @Before
  public void intialize() throws SaveNotAvailableException {
    account = new Account("user@example.com", "demo", "ROLE_USER", "demo");
    account2 = new Account("adri@udc.es", "adri", "ROLE_USER", "adri");
    account3 = new Account("adri@udc.es", "otero", "ROLE_USER", "otero");
    account4 = new Account("otero@udc.es", "adri", "ROLE_USER", "adri");
    account5 = new Account("christian@udc.es", "christian", "ROLE_USER", "christian");
    accountService.save(account5);
  }

  // PR-UN-001
  @Test
  public void testSaveAccount() throws SaveNotAvailableException {
    Account expectedAccount = accountService.save(account);
    assertEquals(expectedAccount.getClass(), Account.class);
    assertEquals(account, expectedAccount);
  }

  // PR-UN-002
  @Test(expected = SaveNotAvailableException.class)
  public void testSaveBlogWithExistingEmail() throws SaveNotAvailableException {
    accountService.save(account2);
    accountService.save(account3);
  }

  // PR-UN-003
  @Test(expected = SaveNotAvailableException.class)
  public void testSaveBlogWithExistingUserName() throws SaveNotAvailableException {
    accountService.save(account2);
    accountService.save(account4);
  }

  // PR-UN-004
  @Test
  public void testShouldInitializeWithTwoDemoUsers() {
    List<Account> accounts = accountRepository.findAll();
    // 1 account que se ha introducido en la BD a través del método intialize() y otros 2 que se
    // meten por defecto en la aplicación
    assertEquals(accounts.size(), 3);
  }

  // PR-UN-005
  @Test(expected = UsernameNotFoundException.class)
  public void testLoadUserWithNotExistingUserName() {
    accountService.loadUserByUsername("christian2@udc.es");
  }

  // PR-UN-006
  @Test
  public void testShouldReturnUserDetails() {
    UserDetails userDetails = accountService.loadUserByUsername("christian@udc.es");
    assertEquals(account5.getEmail(), userDetails.getUsername());
    assertEquals(account5.getPassword(), userDetails.getPassword());
    assertThat(hasAuthority(userDetails, account5.getRole())).isTrue();
  }

  // PR-UN-007
  @Test
  public void testUpdate() throws SaveNotAvailableException {
    account5.setUserName("pruebaUpdate");
    accountService.update(account5);
    assertEquals(account5.getUserName(), "pruebaUpdate");
  }

  private boolean hasAuthority(UserDetails userDetails, String role) {
    return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .anyMatch(isEqual(role));
  }

}