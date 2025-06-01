package com.memo.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlackListRepository extends JpaRepository<TokenBlackList, Long> {
	Optional<TokenBlackList> findByToken(String token);
}
