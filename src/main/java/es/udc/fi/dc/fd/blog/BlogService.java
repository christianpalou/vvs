package es.udc.fi.dc.fd.blog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountRepository;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BlogService {

	@Autowired
	private BlogRepository blogRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Transactional
	public Blog save(Blog blog) throws SaveNotAvailableException {
		if(blogRepository.findOneByName(blog.getName()) != null){
			throw new SaveNotAvailableException("Ya existe un blog con ese nombre");
		}
		blogRepository.save(blog);
		blog.getAccount().addBlog(blog);
		return blog;
	}

	@Transactional
	public Blog update(Blog blog) throws SaveNotAvailableException {
		String newName=blog.getName();
		String oldName=blogRepository.findOne(blog.getBlogId()).getName();
		if(!newName.equals(oldName))
			if(blogRepository.findOneByName(blog.getName()) != null) {
				throw new SaveNotAvailableException("Ya existe un blog con ese nombre");
			}
		blogRepository.save(blog);
		blog.getAccount().addBlog(blog);
		return blog;
	}

	@Transactional
	public void follow(Long accountId, Long blogId) throws FollowException, InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		if (accountId.equals(blog.getAccount().getId())){
			throw new FollowException("No se puede seguir a blog perteneciente");
		}

		if (isFollower(accountId,blogId) || isFollowRequest(accountId,blogId)){
			throw new FollowException("Blog ya seguido o pendiente de seguir");
		}
		else{
			if (blog.isPrivacy()){
				blog.addFollowRequest(account);
			}
			else{
				account.addFollowingBlog(blog);
			}
		}
	}

	@Transactional
	public void unfollow(Long accountId, Long blogId) throws FollowException, InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		if (isFollower(accountId,blogId)){
			blog.getFollowers().remove(account);
			account.getFollowingBlogs().remove(blog);
		}
		else{
			throw new FollowException("Blog no seguido previamente");
		}
	}

	@Transactional
	public void acceptFollowRequest(Long accountId, Long blogId) throws FollowException, InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		if (isFollower(accountId,blogId)){
			throw new FollowException("Blog ya seguido");
		}
		if (accountId.equals(blog.getAccount().getId())){
			throw new FollowException("No se puede seguir a blog perteneciente");
		}
		if (isFollowRequest(accountId,blogId)){
			blog.getFollowRequests().remove(account);
			account.addFollowingBlog(blog);
		}
		else{
			throw new FollowException("Usuario no existente en peticiones pendientes");
		}
	}

	@Transactional
	public void denyFollowRequest(Long accountId, Long blogId) throws FollowException, InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		if (isFollower(accountId,blogId)){
			throw new FollowException("Blog ya seguido");
		}
		if (accountId.equals(blog.getAccount().getId())){
			throw new FollowException("No se puede seguir a blog perteneciente");
		}
		if (isFollowRequest(accountId,blogId)){
			blog.getFollowRequests().remove(account);
		}
		else{
			throw new FollowException("Usuario no existente en peticiones pendientes");
		}
	}

	@Transactional(readOnly=true)
	public Blog findBlogByName(String name){
		return blogRepository.findOneByName(name);
	}

	@Transactional(readOnly=true)
	public List<Blog> findBlogs(String name){
		return blogRepository.findByNameContaining(name);
	}

	@Transactional(readOnly=true)
	public boolean isFollower(Long accountId, Long blogId) throws InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		for(Account follower: blog.getFollowers()){
			if (follower.getId().equals(accountId)){
				return true;
			}
		}
		return false;
	}

	@Transactional(readOnly=true)
	public boolean isFollowRequest(Long accountId, Long blogId) throws InstanceNotFoundException{
		Blog blog = blogRepository.findOne(blogId);
		Account account = accountRepository.findOne(accountId);
		if (blog == null){
			throw new InstanceNotFoundException("El blog no existe");
		}
		if (account==null){
			throw new InstanceNotFoundException("La cuenta no existe");
		}
		for(Account request: blog.getFollowRequests()){
			if (request.getId().equals(accountId)){
				return true;
			}
		}
		return false;

	}

}