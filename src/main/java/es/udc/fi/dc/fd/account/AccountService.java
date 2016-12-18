package es.udc.fi.dc.fd.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import javax.annotation.PostConstruct;

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

  /**
   * Función que almacena los datos relativos a una cuenta en base de datos.
   *
   * @param account
   *          Este parámetro representa el objeto Account que se quiere almacenar
   * @return La cuenta resultante, despues de almacenarla
   * @exception SaveNotAvailableException
   *              excepción saveNotAvailable
   */
  @Transactional
  public Account save(Account account) throws SaveNotAvailableException {
    if (accountRepository.findOneByEmail(account.getEmail()) != null) {
      throw new SaveNotAvailableException("Ya existe una cuenta con ese email");
    }
    if (accountRepository.findOneByUserName(account.getUserName()) != null) {
      throw new SaveNotAvailableException("Ya existe una cuenta con ese nombre de usuario");
    }
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    accountRepository.save(account);
    return account;
  }

  /**
   * Función que actualiza los datos relativos a una cuenta en base de datos.
   *
   * @param account
   *          Este parámetro representa el nuevo objeto Account que se quiere almacenar
   * @return La cuenta resultante, despues de actualizarla
   * @exception SaveNotAvailableException
   *              excepción saveNotAvailable
   */
  @Transactional
  public Account update(Account account) throws SaveNotAvailableException {
    if (!account.getEmail().equals(accountRepository.findOne(account.getId()).getEmail())) {
      if (accountRepository.findOneByEmail(account.getEmail()) != null) {
        throw new SaveNotAvailableException("Ya existe una cuenta con ese email");
      }
    }
    if (!account.getUserName().equals(accountRepository.findOne(account.getId()).getUserName())) {
      if (accountRepository.findOneByUserName(account.getUserName()) != null) {
        throw new SaveNotAvailableException("Ya existe una cuenta con ese nombre de usuario");
      }
    }
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    accountRepository.save(account);
    return account;
  }

  /**
   * Funcion que carga un usuario a partir de su email
   * 
   * @param username
   *          Este parámetro representa el email de un usuario
   * @return El usuario creado si existe una cuenta asociada a ese email
   * @exception UsernameNotFoundException
   *              excepción de nombre de usuario no encontrado
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountRepository.findOneByEmail(username);
    if (account == null) {
      throw new UsernameNotFoundException("user not found");
    }
    return createUser(account);
  }

  /**
   * Procedimiento que loggea a un usuario para introducir al usuario autenticado en el contexto de
   * la aplicación.
   *
   * @param account
   *          Este parámetro representa el objeto Account que se quiere loggear
   */
  public void signin(Account account) {
    SecurityContextHolder.getContext().setAuthentication(authenticate(account));
  }

  /**
   * Procedimiento que autentica a un usuario
   * 
   * @param account
   *          Este parámetro representa el objeto Account que se quiere autenticar
   * @return Usuario autenticado
   */
  private Authentication authenticate(Account account) {

    return new UsernamePasswordAuthenticationToken(createUser(account), null,
        Collections.singleton(createAuthority(account)));
  }

  /**
   * Funcion que crea un usuario
   * 
   * @param account
   *          Este parámetro representa el objeto Account que contiene los datos necesarios para
   *          crear un usuario
   * @return El usuario creado
   */
  private User createUser(Account account) {
    return new User(account.getEmail(), account.getPassword(),
        Collections.singleton(createAuthority(account)));
  }

  /**
   * Funcion que asigna un rol un usuario
   * 
   * @param account
   *          Este parámetro representa el objeto Account
   * @return El usuario con el nuevo rol
   */
  private GrantedAuthority createAuthority(Account account) {
    return new SimpleGrantedAuthority(account.getRole());
  }

}
