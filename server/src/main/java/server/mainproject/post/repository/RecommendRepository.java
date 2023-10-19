package server.mainproject.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.mainproject.post.entity.Recommend;

import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    @Query("select r from Recommend r where r.member.memberId = :mId and r.post.postId = :pId")
    Optional<Recommend> findByMemberRecommend (@Param("mId") long memberId, @Param("pId") long postId);
}
