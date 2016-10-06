package es.udc.fi.dc.fd.blog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>{
	Blog findOneByName(String name);
	List<Blog> findByNameContaining(String name);
}