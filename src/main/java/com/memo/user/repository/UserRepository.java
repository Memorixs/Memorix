package com.memo.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.memo.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByProviderId(String providerId);
	User findByEmailEquals(String email); //회원가입 시 중복체크, null이면 회원가입 진행
	User findByUsernameEquals(String username);
}
