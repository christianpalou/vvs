package es.udc.fi.dc.fd.account;

import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
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
import es.udc.fi.dc.fd.config.ApplicationConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationConfig.class})
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
	
	@Before
	public void intialize() throws SaveNotAvailableException{
		account = new Account("user@example.com", "demo", "ROLE_USER", "demo");
		account2 = new Account("adri@udc.es", "adri", "ROLE_USER", "adri");
		account3 = new Account("adri@udc.es", "adri", "ROLE_USER", "adri");
		accountService.save(account);
		accountService.save(account2);
	}
	
	//PR-UN-028
	@Test
	public void testSaveAccount() throws SaveNotAvailableException{
		Account account2 = accountService.save(account);
		//assertNotNull(account2);
		assertEquals(account2.getClass(),Account.class);
		assertEquals(account.getEmail(), account2.getEmail());
		assertEquals(account.getUserName(), account2.getUserName());
	}

	//PR-UN-029
	@Test(expected = SaveNotAvailableException.class)
	public void testSaveBlogWithExistingEmail() throws SaveNotAvailableException{
		accountService.save(account3);
	}

	//PR-UN-030
	@Test(expected = SaveNotAvailableException.class)
	public void testSaveBlogWithExistingUserName() throws SaveNotAvailableException{
		accountService.save(account3);
	}

	//PR-UN-031
	@Test
	public void shouldInitializeWithTwoDemoUsers() {
		List<Account> accounts = accountRepository.findAll();
		// assert
		// 2 accounts que se han introducido en la BD a través del método intialize() y otros 2 que se meten por defecto en la aplicación
		assertEquals(accounts.size(), 4);
	}

	//PR-UN-032
	@Test(expected=UsernameNotFoundException.class)
	public void shouldThrowExceptionWhenUserNotFound() {
		// arrange
		accountRepository.findOneByEmail("user@example.com");
		// act
		accountService.loadUserByUsername("user2@example.com");
	}

	//PR-UN-033
	@Test
	public void shouldReturnUserDetails() throws SaveNotAvailableException {
		// arrange
		accountRepository.findOneByEmail("user@example.com");

		// act
		UserDetails userDetails = accountService.loadUserByUsername("user@example.com");

		// assert
		assertThat(account.getEmail()).isEqualTo(userDetails.getUsername());
		assertThat(account.getPassword()).isEqualTo(userDetails.getPassword());
		assertThat(hasAuthority(userDetails, account.getRole())).isTrue();
	}

	private boolean hasAuthority(UserDetails userDetails, String role) {
		return userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(isEqual(role));
	}
	
	@After
	public void endTest(){
		account = null;
	}
}