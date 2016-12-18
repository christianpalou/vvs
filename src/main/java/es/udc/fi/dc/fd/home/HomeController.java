package es.udc.fi.dc.fd.home;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class HomeController {

  @Autowired
  private AccountRepository accountRepository;
/**
 * 
 * @param model
 * @param principal
 * @return
 */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index(Model model, Principal principal) {
    if (principal != null) {
      Account account = accountRepository.findOneByEmail(principal.getName());
      model.addAttribute("account", account);
      return "home/homeSignedIn";
    } else {
      return "home/homeNotSignedIn";
    }
  }
}