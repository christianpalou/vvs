package es.udc.fi.dc.fd.blog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountService;
import es.udc.fi.dc.fd.config.ApplicationConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@WebAppConfiguration
@Transactional
public class BlogRepositoryTest {

  @Autowired
  private BlogService blogService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private BlogRepository blogRepository;

  Account account;
  Account account2;
  Account account3;
  Blog blog1;
  Blog blog2;
  Blog blog3;
/**
 * 
 * @throws es.udc.fi.dc.fd.account.SaveNotAvailableException
 * @throws SaveNotAvailableException
 */
  @Before
  public void initialice()
      throws es.udc.fi.dc.fd.account.SaveNotAvailableException, SaveNotAvailableException {
    account = accountService.save(new Account("adri@udc.es", "adri", "ROLE_USER", "adri"));

    blog1 = blogService
        .save(new Blog("MyBlog", "AdriBlog", "This is the blog of Adri", false, account));
    blog2 = blogService
        .save(new Blog("YourBlog", "AdriBlog2", "This is the second blog of Adri", true, account));

    blog3 = new Blog("Life", "life love you", "This is the blog of my life", false, account);

  }

  // PR-UN-001
  @Test
  public void testSaveBlog() {
    Blog blog = blogRepository.save(blog3);
    assertNotNull(blog);
    assertEquals(blog.getClass(), Blog.class);
  }

  // PR-UN-002
  @Test
  public void testfindOneByNameContaining() {
    List<Blog> blogs = blogRepository.findByNameContaining("Blog");

    List<Blog> expectedBlogs = new ArrayList<Blog>();
    expectedBlogs.add(blog1);
    expectedBlogs.add(blog2);

    assertEquals(blogs, expectedBlogs);
  }

  // PR-UN-003
  @Test
  public void testfindOneByName() {
    Blog expectedblog = blogRepository.findOneByName("MyBlog");
    assertEquals(blog1, expectedblog);

  }

}