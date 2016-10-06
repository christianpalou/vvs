package es.udc.fi.dc.fd.finds;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountRepository;
import es.udc.fi.dc.fd.blog.Blog;
import es.udc.fi.dc.fd.blog.BlogService;

@Controller
public class FindsController {

	@Autowired
	private BlogService blogService;

	@Autowired
	private AccountRepository accountRepository;

	@RequestMapping(value = "finds", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public String findBlogs(@RequestParam(required = false, value = "keywords") String keywords, Model model, Principal principal) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account",account);
		model.addAttribute("search", keywords);
		List<Blog> foundBlogs= blogService.findBlogs(keywords);
		model.addAttribute("foundBlogs", foundBlogs);
		return "finds/finds";
	}
}