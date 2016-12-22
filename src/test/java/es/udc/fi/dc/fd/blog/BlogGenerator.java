package es.udc.fi.dc.fd.blog;

import org.springframework.beans.factory.annotation.Autowired;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountGenerator;
import es.udc.fi.dc.fd.account.AccountService;
import es.udc.fi.dc.fd.account.SaveNotAvailableException;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

public class BlogGenerator implements Generator<Blog> {

  @Autowired
  private AccountService accountService;

  Generator<String> name = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 6);
  Generator<String> title = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 6);
  Generator<String> description = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 20);

  AccountGenerator account = new AccountGenerator();

  @Override
  public Blog next() {
    return new Blog(name.next(), title.next(), description.next(), false, account.next());
  }
}
