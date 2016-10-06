package es.udc.fi.dc.fd.account;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

import es.udc.fi.dc.fd.account.SaveNotAvailableException;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountService implements UserDetailsService {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostConstruct	
	protected void initialize() throws SaveNotAvailableException {
		save(new Account("user@udc.es", "demo", "ROLE_USER", "user"));
		save(new Account("admin@udc.es", "admin", "ROLE_ADMIN", "admin"));
	}

	@Transactional
	public Account save(Account account) throws SaveNotAvailableException {
		if(accountRepository.findOneByEmail(account.getEmail()) != null){
			throw new SaveNotAvailableException("Ya existe una cuenta con ese email");
		}
		if(accountRepository.findOneByUserName(account.getUserName()) != null) {
			throw new SaveNotAvailableException("Ya existe una cuenta con ese nombre de usuario");
		}
		account.setPassword(passwordEncoder.encode(account.getPassword()));
		accountRepository.save(account);
		return account;
	}

	@Transactional
	public Account update(Account account) throws SaveNotAvailableException {
		if(!account.getEmail().equals(accountRepository.findOne(account.getId()).getEmail()))
			if(accountRepository.findOneByEmail(account.getEmail()) != null) {
				throw new SaveNotAvailableException("Ya existe una cuenta con ese email");
			}
		if(!account.getUserName().equals(accountRepository.findOne(account.getId()).getUserName()))
			if(accountRepository.findOneByUserName(account.getUserName()) != null) {
				throw new SaveNotAvailableException("Ya existe una cuenta con ese nombre de usuario");
			}	
		account.setPassword(passwordEncoder.encode(account.getPassword()));
		accountRepository.save(account);
		return account;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = accountRepository.findOneByEmail(username);
		if(account == null) {
			throw new UsernameNotFoundException("user not found");
		}
		return createUser(account);
	}

	public void signin(Account account) {
		SecurityContextHolder.getContext().setAuthentication(authenticate(account));
	}

	private Authentication authenticate(Account account) {
		return new UsernamePasswordAuthenticationToken(createUser(account), null, Collections.singleton(createAuthority(account)));		
	}

	private User createUser(Account account) {
		return new User(account.getEmail(), account.getPassword(), Collections.singleton(createAuthority(account)));
	}

	private GrantedAuthority createAuthority(Account account) {
		return new SimpleGrantedAuthority(account.getRole());
	}

}
