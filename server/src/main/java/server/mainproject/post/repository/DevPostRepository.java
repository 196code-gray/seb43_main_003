package server.mainproject.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import server.mainproject.post.entity.DevPost;

import java.util.List;
public interface DevPostRepository extends JpaRepository<DevPost, Long> {
    List<DevPost> findBySorta(String sorta);
}
