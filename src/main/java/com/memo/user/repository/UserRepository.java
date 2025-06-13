package com.memo.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.memo.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByProviderId(String providerId);
	User findByEmailEquals(String email); //회원가입 시 중복체크, null이면 회원가입 진행
	User findByUsernameEquals(String username);

	//네이티브 쿼리로 update하는거랑 더티체킹이랑 뭔차이?  -> 영속성 관리
	@Modifying
	@Query("update User u set u.isDeleted = true where u.id = :id")
	void softDeleteById(Long id);
}
