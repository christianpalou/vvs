package es.udc.fi.dc.fd.account;

import org.springframework.beans.factory.annotation.Autowired;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

public class AccountGenerator implements Generator<Account> {

  Generator<String> mail = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 6);
  Generator<String> user = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 6);
  Generator<String> pass = PrimitiveGenerators.strings("abcdefghijklmnñopqrstuvwxyz", 1, 6);

  @Override
  public Account next() {

    return new Account(mail.next() + "@udc.es", pass.next(), "ROLE_USER", user.next());

  }

}
