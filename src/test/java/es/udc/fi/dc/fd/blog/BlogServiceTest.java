package es.udc.fi.dc.fd.blog;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountService;
import es.udc.fi.dc.fd.config.ApplicationConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationConfig.class})
@WebAppConfiguration
@Transactional
public class BlogServiceTest {

	private final long NON_EXISTENT_ID = -1;

	@Autowired
	private BlogService blogService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private BlogRepository blogRepository;

	@Test
	public void testSaveBlog() throws SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		Blog foundBlog=blogRepository.findOneByName(blog.getName());
		assertEquals(blog, foundBlog);
	}

	@Test(expected = SaveNotAvailableException.class)
	public void testSaveBlogWithExistingName() throws SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.save(new Blog("MyBlog", "BraisBlog", "This is the blog of Brais", false, account));

	}

	@Test
	public void testUpdateBlog() throws SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blog.setName("YourBlog");
		blog.setTitle("BraisBlog");
		Blog updatedBlog=blogService.update(blog);
		Blog foundBlog=blogRepository.findOneByName(blog.getName());
		assertEquals(updatedBlog, foundBlog);
		blog.setTitle("ChristianBlog");
		updatedBlog=blogService.update(blog);
		foundBlog=blogRepository.findOneByName(blog.getName());
		assertEquals(updatedBlog, foundBlog);
	}

	@Test
	public void testFollow() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Account account3=accountService.save(new Account("christian@udc.es", "christian", "ROLE_USER", "christian"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.follow(account2.getId(), blog.getBlogId());
		blogService.follow(account3.getId(), blog.getBlogId());
		blogService.follow(account3.getId(), blog2.getBlogId());
		List<Account> expectedAccounts = new ArrayList<Account>();
		expectedAccounts.add(account2);
		expectedAccounts.add(account3);
		List<Account> expectedRequests = new ArrayList<Account>();
		expectedRequests.add(account3);
		List<Blog> expectedBlogs = new ArrayList<Blog>();
		expectedBlogs.add(blog);
		assertTrue(expectedAccounts.equals(blog.getFollowers()));
		assertTrue(expectedBlogs.equals(account2.getFollowingBlogs()));
		assertTrue(expectedBlogs.equals(account3.getFollowingBlogs()));
		assertTrue(expectedRequests.equals(blog2.getFollowRequests()));
	}

	@Test
	public void testFollowNotAvailable() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));

		//Prueba seguir a blog propio
		boolean exceptionCatched = false;
		try {
			blogService.follow(account.getId(), blog.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);

		//Prueba seguir a blog ya seguido
		blogService.follow(account2.getId(), blog.getBlogId());
		exceptionCatched = false;
		try {
			blogService.follow(account2.getId(), blog.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);

		//Prueba seguir a blog con petición pendiente
		blogService.follow(account2.getId(), blog2.getBlogId());
		exceptionCatched = false;
		try {
			blogService.follow(account2.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testFollowNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.follow(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testFollowWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.follow(NON_EXISTENT_ID, blog.getBlogId());
	}

	@Test
	public void testUnfollow() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Account account3=accountService.save(new Account("christian@udc.es", "christian", "ROLE_USER", "christian"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.follow(account2.getId(), blog.getBlogId());
		blogService.follow(account3.getId(), blog.getBlogId());
		List<Account> expectedAccounts = new ArrayList<Account>();
		expectedAccounts.add(account2);
		expectedAccounts.add(account3);
		List<Blog> expectedBlogs = new ArrayList<Blog>();
		expectedBlogs.add(blog);
		assertTrue(expectedAccounts.equals(blog.getFollowers()));
		assertTrue(expectedBlogs.equals(account2.getFollowingBlogs()));
		assertTrue(expectedBlogs.equals(account3.getFollowingBlogs()));

		blogService.unfollow(account3.getId(), blog.getBlogId());
		expectedAccounts.remove(account3);
		expectedBlogs.remove(blog);
		assertTrue(expectedAccounts.equals(blog.getFollowers()));
		assertTrue(expectedBlogs.equals(account3.getFollowingBlogs()));
	}

	@Test
	public void testUnfollowNotAvailable() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));

		//Prueba dejar de seguir a blog no seguido anteriormente
		boolean exceptionCatched = false;
		try {
			blogService.unfollow(account2.getId(), blog.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testUnfollowNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.unfollow(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testUnfollowWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.unfollow(NON_EXISTENT_ID, blog.getBlogId());
	}

	@Test
	public void testAcceptFollowRequest() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.follow(account2.getId(), blog2.getBlogId());
		blogService.acceptFollowRequest(account2.getId(), blog2.getBlogId());
		List<Account> expectedAccounts = new ArrayList<Account>();
		expectedAccounts.add(account2);
		List<Blog> expectedBlogs = new ArrayList<Blog>();
		expectedBlogs.add(blog2);
		assertTrue(expectedAccounts.equals(blog2.getFollowers()));
		assertTrue(expectedBlogs.equals(account2.getFollowingBlogs()));
		assertTrue(blog2.getFollowRequests().isEmpty());
	}

	@Test
	public void testAcceptFollowRequestNotAvailable() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));

		//Prueba aceptar peticion de cuenta seguidora
		blogService.follow(account2.getId(), blog2.getBlogId());
		blogService.acceptFollowRequest(account2.getId(), blog2.getBlogId());
		boolean exceptionCatched = false;
		try {
			blogService.acceptFollowRequest(account2.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
		blogService.unfollow(account2.getId(), blog2.getBlogId());

		//Prueba aceptar peticion de cuenta propietaria
		exceptionCatched = false;
		try {
			blogService.acceptFollowRequest(account.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);

		//Prueba aceptar peticion de cuenta sin petición pendiente
		exceptionCatched = false;
		try {
			blogService.acceptFollowRequest(account2.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testAcceptFollowRequestNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.acceptFollowRequest(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testAcceptFollowRequestWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.acceptFollowRequest(NON_EXISTENT_ID, blog2.getBlogId());
	}

	@Test
	public void testDenyFollowRequest() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.follow(account2.getId(), blog2.getBlogId());
		blogService.denyFollowRequest(account2.getId(), blog2.getBlogId());
		assertTrue(blog2.getFollowers().isEmpty());
		assertTrue(account2.getFollowingBlogs().isEmpty());
		assertTrue(blog2.getFollowRequests().isEmpty());
	}

	@Test
	public void testDenyFollowRequestNotAvailable() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));

		//Prueba denegar peticion de cuenta seguidora
		blogService.follow(account2.getId(), blog2.getBlogId());
		blogService.acceptFollowRequest(account2.getId(), blog2.getBlogId());
		boolean exceptionCatched = false;
		try {
			blogService.denyFollowRequest(account2.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
		blogService.unfollow(account2.getId(), blog2.getBlogId());

		//Prueba denegar peticion de cuenta propietaria
		exceptionCatched = false;
		try {
			blogService.denyFollowRequest(account.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);

		//Prueba denegar peticion de cuenta sin petición pendiente
		exceptionCatched = false;
		try {
			blogService.denyFollowRequest(account2.getId(), blog2.getBlogId());
		} catch (FollowException e) {
			exceptionCatched = true;
		}
		assertTrue(exceptionCatched);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testDenyFollowRequestNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.denyFollowRequest(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testDenyFollowRequestWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.denyFollowRequest(NON_EXISTENT_ID, blog2.getBlogId());
	}

	@Test
	public void testFindBlogByName() throws SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		Blog foundBlog= blogService.findBlogByName(blog.getName());
		assertEquals(blog,foundBlog);
	}

	@Test
	public void testFindBlogs() throws SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		List<Blog> foundBlogs= blogService.findBlogs("zzz");
		assertTrue(foundBlogs.isEmpty());
		foundBlogs=blogService.findBlogs("My");
		List<Blog> expectedBlogs= new ArrayList<Blog>();
		expectedBlogs.add(blog);
		assertEquals(expectedBlogs,foundBlogs);
		foundBlogs=blogService.findBlogs("Blog");
		expectedBlogs.add(blog2);
		assertEquals(expectedBlogs,foundBlogs);	
	}

	@Test
	public void testIsFollower() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.follow(account2.getId(), blog.getBlogId());
		assertTrue(blogService.isFollower(account2.getId(), blog.getBlogId()));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testIsFollowerNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.isFollower(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testIsFollowerWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog =blogService.save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
		blogService.isFollower(NON_EXISTENT_ID, blog.getBlogId());
	}

	@Test
	public void testIsFollowRequest() throws InstanceNotFoundException, FollowException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Account account2=accountService.save(new Account("brais@udc.es", "brais", "ROLE_USER", "brais"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.follow(account2.getId(), blog2.getBlogId());
		assertTrue(blogService.isFollowRequest(account2.getId(), blog2.getBlogId()));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testIsFollowRequestNoExistentBlog() throws FollowException, InstanceNotFoundException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		blogService.isFollowRequest(account.getId(), NON_EXISTENT_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testIsFollowRequestWithNoExistentAccount() throws FollowException, InstanceNotFoundException, SaveNotAvailableException, es.udc.fi.dc.fd.account.SaveNotAvailableException{
		Account account=accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));
		Blog blog2 =blogService.save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));
		blogService.isFollowRequest(NON_EXISTENT_ID, blog2.getBlogId());
	}
}