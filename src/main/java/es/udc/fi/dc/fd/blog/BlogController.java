package es.udc.fi.dc.fd.blog;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountRepository;

@Controller
public class BlogController {

	@Autowired
	private CreateBlogFormValidator createBlogValidator;

	@Autowired
	private BlogService blogService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private BlogRepository blogRepository;

	@RequestMapping(value = "/createBlog", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String createBlog(Principal principal, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		model.addAttribute(new CreateBlogForm());
		return "blog/createBlog";
	}

	@RequestMapping(value = "/createBlog", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String createBlog(@Valid @ModelAttribute CreateBlogForm createBlogForm, Errors errors, RedirectAttributes ra,
			Principal principal, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		createBlogValidator.validate(createBlogForm, errors);
		if (errors.hasErrors()) {
			return "blog/createBlog";
		}
		Blog blog1 = createBlogForm.createBlog();
		try {
			blog1.setAccount(account);
			blogService.save(blog1);
		} catch (SaveNotAvailableException e) {
			return "blog/createBlog";
		}
		return "redirect:/";
	}

	@RequestMapping(value = "/blog/{id}", method = RequestMethod.GET)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String blog(Principal principal, @PathVariable Long id, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);

		Blog blog = blogRepository.findOne(id);
		model.addAttribute(blog);

		boolean followButton = true;
		boolean isOwner = false;
		if (account.getId() == blog.getAccount().getId()) {
			followButton = false;
			isOwner = true;
		}
		boolean unfollowButton = false;
		try {
			if (blogService.isFollower(account.getId(), id)) {
				unfollowButton = true;
				followButton = false;
			}
			if (blogService.isFollowRequest(account.getId(), id)) {
				followButton = false;
			}
		} catch (InstanceNotFoundException e) {
			return "blog/blog";
		}
		model.addAttribute("followButton", followButton);
		model.addAttribute("unfollowButton", unfollowButton);
		model.addAttribute("isOwner", isOwner);
		return "blog/blog";
	}

	@RequestMapping(value = "blog/{id}/followers", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String followers(Principal principal, @PathVariable("id") Long id, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		Blog blog = blogRepository.findOne(id);
		model.addAttribute("blog", blog);
		return "blog/followers";
	}

	@RequestMapping(value = "blog/{id}/followRequests", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String followRequests(Principal principal, @PathVariable("id") Long id, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		model.addAttribute("account", account);
		Blog blog = blogRepository.findOne(id);
		boolean isOwner = false;
		if (account.getId() == blog.getAccount().getId()) {
			isOwner = true;
		}
		model.addAttribute("blog", blog);
		model.addAttribute("isOwner", isOwner);
		return "blog/followRequests";
	}

	@RequestMapping(value = "blog/{blogId}", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public String follow(Principal principal, @PathVariable("blogId") Long blogId,
			@RequestParam(required = false, value = "follow") String follow,
			@RequestParam(required = false, value = "unfollow") String unfollow, Model model) {
		Account account = accountRepository.findOneByEmail(principal.getName());
		try {
			if (follow != null && follow.equalsIgnoreCase("follow")) {
				blogService.follow(account.getId(), blogId);
			}
			if (unfollow != null && unfollow.equalsIgnoreCase("unfollow")) {
				blogService.unfollow(account.getId(), blogId);
			}
		} catch (FollowException | InstanceNotFoundException e) {
			return "blog/blog";
		}
		return "redirect:/blog/" + blogId;
	}

	@RequestMapping(value = "blog/{blogId}/followRequests/{id}", method = RequestMethod.POST)
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
			return "blog/followRequests";
		}
		return "redirect:/blog/" + blogId + "/followRequests";
	}

}