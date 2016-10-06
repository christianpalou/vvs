package es.udc.fi.dc.fd.account;

import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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

	@Test
	public void testSaveAccount() throws SaveNotAvailableException{
		Account account = accountService.save(new Account("user@example.com", "demo", "ROLE_USER", "demo"));
		Account foundAccount=accountRepository.findOneByEmail("user@example.com");
		assertEquals(account, foundAccount);
	}

	@Test(expected = SaveNotAvailableException.class)
	public void testSaveBlogWithExistingEmail() throws SaveNotAvailableException{
		accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adrilpz"));
	}

	@Test(expected = SaveNotAvailableException.class)
	public void testSaveBlogWithExistingUserName() throws SaveNotAvailableException{
		accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		accountService.save(new Account("adrilpz@udc.es", "adri", "ROLE_USER", "adri"));
	}

	@Test
	public void shouldInitializeWithTwoDemoUsers() {
		// assert
		assertEquals(accountRepository.findAll().size(), 2);
	}

	@Test(expected=UsernameNotFoundException.class)
	public void shouldThrowExceptionWhenUserNotFound() {
		// arrange
		accountRepository.findOneByEmail("user@example.com");
		// act
		accountService.loadUserByUsername("user@example.com");
	}

	@Test
	public void shouldReturnUserDetails() throws SaveNotAvailableException {
		// arrange
		Account demoUser = new Account("user@example.com", "demo", "ROLE_USER", "demo");
		accountService.save(demoUser);
		accountRepository.findOneByEmail("user@example.com");

		// act
		UserDetails userDetails = accountService.loadUserByUsername("user@example.com");

		// assert
		assertThat(demoUser.getEmail()).isEqualTo(userDetails.getUsername());
		assertThat(demoUser.getPassword()).isEqualTo(userDetails.getPassword());
		assertThat(hasAuthority(userDetails, demoUser.getRole())).isTrue();
	}

	private boolean hasAuthority(UserDetails userDetails, String role) {
		return userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(isEqual(role));
	}
}