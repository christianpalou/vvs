package es.udc.fi.dc.fd.account;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.udc.fi.dc.fd.blog.Blog;
import es.udc.fi.dc.fd.blog.BlogRepository;
import es.udc.fi.dc.fd.blog.BlogService;
import es.udc.fi.dc.fd.blog.FollowException;
import es.udc.fi.dc.fd.blog.InstanceNotFoundException;
import es.udc.fi.dc.fd.blog.SaveNotAvailableException;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AccountController {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private BlogService blogService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private BlogRepository blogRepository;

	@Autowired
	private AccountConfigurationFormValidator accountConfigurationValidator;

	@Autowired
	private BlogConfigurationFormValidator blogConfigurationValidator;

	@Autowired
	public AccountController(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Value("${files.directory}")
	private String path;

	@RequestMapping(value = "account/configuration", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String configurationAccount(Principal principal, Model model) throws IOException {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		model.addAttribute(new AccountConfigurationForm());
		return "account/configuration";
	}

	@RequestMapping(value = "account/configuration", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String configurationAccount(@Valid @ModelAttribute AccountConfigurationForm accountConfigurationForm,
			@RequestParam("file") MultipartFile myFile, Errors errors, RedirectAttributes ra, Principal principal, Model model)
					throws IllegalStateException, IOException {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		if (!accountConfigurationForm.getUserName().equals(account.getUserName()))
			accountConfigurationValidator.validate(accountConfigurationForm, errors);
		if (errors.hasErrors()) {
			return "/account/configuration";
		}

		if (!accountConfigurationForm.getPassword().isEmpty())
			account.setPassword(accountConfigurationForm.getPassword());
		if (!accountConfigurationForm.getUserName().isEmpty() && !accountConfigurationForm.getUserName().equals(account.getUserName()))
			account.setUserName(accountConfigurationForm.getUserName());

		if (!myFile.isEmpty()) {
			String[] parts = myFile.getOriginalFilename().split("\\.");
			if (parts[1].equals("jpg")) {
				account.setImage(true);
				File destination = new File(path + account.getId() + "-account." + parts[1]);
				myFile.transferTo(destination);
			}
			else{
				String message="Archivo no JPG seleccionado";
				model.addAttribute("message",message);
				return "account/error";
			}
		}

		try {
			accountService.update(account);
		} catch (es.udc.fi.dc.fd.account.SaveNotAvailableException e) {
			account = accountRepository.findOneByEmail(principal.getName());
			model.addAttribute("account", account);
			return "/account/configuration";
		}

		if (!(account.getEmail().equals(principal.getName()))){
			SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
			accountService.signin(account);
		}
		return "redirect:/account/configuration";
	}

	@RequestMapping(value = "/account/configuration/blogconfiguration/{id}", method = RequestMethod.GET)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String configurationBlog(Principal principal, @PathVariable Long id, Model model) throws IOException {

		Account account = accountRepository.findOneByEmail(principal.getName());
		Blog blog = blogRepository.findOne(id);
		model.addAttribute("account", account);
		model.addAttribute("blog", blog);
		model.addAttribute(new BlogConfigurationForm());
		return "account/blogconfiguration";
	}

	@RequestMapping(value = "/account/configuration/blogconfiguration/{id}", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String configurationBlog(@Valid @ModelAttribute BlogConfigurationForm blogConfigurationForm,
			@RequestParam("file") MultipartFile myFile, Errors errors, RedirectAttributes ra, Principal principal,
			@PathVariable Long id, Model model) throws IllegalStateException, IOException {
		Account account = accountRepository.findOneByEmail(principal.getName());
		Blog blog = blogRepository.findOne(id);
		model.addAttribute("account", account);
		model.addAttribute("blog", blog);
		if (!blogConfigurationForm.getName().equals(blog.getName()))
			blogConfigurationValidator.validate(blogConfigurationForm, errors);
		if (errors.hasErrors()) {
			return "/account/blogconfiguration";
		}
		if (!blogConfigurationForm.getName().isEmpty() && !blogConfigurationForm.getName().equals(blog.getName()))
			blog.setName(blogConfigurationForm.getName());
		if (!blogConfigurationForm.getTitle().isEmpty())
			blog.setTitle(blogConfigurationForm.getTitle());
		if (!blogConfigurationForm.getDescription().isEmpty())
			blog.setDescription(blogConfigurationForm.getDescription());
		if (blogConfigurationForm.isPrivacy())
			blog.setPrivacy(true);
		else
			blog.setPrivacy(false);

		if (!myFile.isEmpty()) {
			String[] parts = myFile.getOriginalFilename().split("\\.");
			if (parts[1].equals("jpg")) {
				blog.setImage(true);
				File destination = new File(path + account.getId() + "-" + blog.getBlogId() + "-blog." + parts[1]);
				myFile.transferTo(destination);
			}
			else{
				String message="Archivo no JPG seleccionado";
				model.addAttribute("message",message);
				return "account/error";
			}
		}
		try {
			blogService.update(blog);
		} catch (SaveNotAvailableException e) {
			return "/account/blogconfiguration";
		}

		return "redirect:/account/configuration/blogconfiguration/"+id.toString();

	}

	@RequestMapping(value = "account/{id}/followingBlogs", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String followingBlogs(@PathVariable("id") Long id, Model model, Principal principal) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		Account otherAccount = accountRepository.findOne(id);
		model.addAttribute("otheraccount", otherAccount);
		boolean isUserFollowingBlogs = false;
		if (account.getId() == otherAccount.getId()) {
			isUserFollowingBlogs = true;
		}
		model.addAttribute("isUserFollowingBlogs", isUserFollowingBlogs);
		return "account/followingBlogs";
	}

	@RequestMapping(value = "account/{id}/followingBlogs/{blogId}", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String unfollow(@PathVariable("id") Long id, @PathVariable("blogId") Long blogId, Model model,
			Principal principal) {
		Account otherAccount = accountRepository.findOne(id);
		model.addAttribute("account", otherAccount);
		try {
			blogService.unfollow(id, blogId);
		} catch (FollowException | InstanceNotFoundException e) {
			return "account/followingBlogs";
		}
		return "redirect:/account/" + id + "/followingBlogs";
	}

	@RequestMapping(value = "account/{id}/details", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String acccountDetails(@PathVariable("id") Long id, Model model, Principal principal) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		Account otherAccount = accountRepository.findOne(id);
		model.addAttribute("otheraccount", otherAccount);
		return "account/details";
	}

	@RequestMapping(value = "account/notifications", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String accountNotifications(Principal principal, Model model){
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		return "account/notifications";
	}

	@RequestMapping(value = "account/notifications/{blogId}/{id}", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String answerFollowRequest(@PathVariable("blogId") Long blogId,
			@RequestParam(required = false, value = "accept") String accept,
			@RequestParam(required = false, value = "deny") String deny, @PathVariable("id") Long id,
			Principal principal, Model model) {
		try {
			if (accept != null && accept.equalsIgnoreCase("accept")) {
				blogService.acceptFollowRequest(id, blogId);
			}
			if (deny != null && deny.equalsIgnoreCase("deny")) {
				blogService.denyFollowRequest(id, blogId);
			}
		} catch (FollowException | InstanceNotFoundException e) {
			return "account/notifications";
		}
		return "redirect:/account/notifications";
	}
}
