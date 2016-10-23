package es.udc.fi.dc.fd.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import es.udc.fi.dc.fd.config.ApplicationConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@WebAppConfiguration
@Transactional
public class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private AccountService accountService;
	
	Account account;
	Account account2;
	
	@Before
	public void initialice() throws SaveNotAvailableException{
		account = new Account("adri@udc.es", "adri", "ROLE_USER", "adri");
		account2 = new Account("otero@udc.es", "otero", "ROLE_USER", "otero");
		accountService.save(account2);
	}
	
	// PR-UN-001
	@Test
	public void testSaveAccount(){
		Account expectedAccount = accountRepository.save(account);
		assertNotNull(expectedAccount);
		assertEquals(expectedAccount.getClass(), Account.class);
	}
	
	// PR-UN-002
	@Test
	public void testFindOneByEmail() {
		Account expectedAccount = accountRepository.findOneByEmail("otero@udc.es");
		assertEquals(expectedAccount,account2);
	}
	
	// PR-UN-003
	@Test
	public void testFindOneByUserName(){
		Account expectedAccount = accountRepository.findOneByUserName("otero");
		assertEquals(expectedAccount,account2);
	}
}
