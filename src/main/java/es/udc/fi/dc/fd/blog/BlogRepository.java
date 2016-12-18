package es.udc.fi.dc.fd.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

  Blog findOneByName(String name);

  List<Blog> findByNameContaining(String name);
}