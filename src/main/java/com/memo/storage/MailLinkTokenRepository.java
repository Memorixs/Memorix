package com.memo.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLinkTokenRepository extends JpaRepository<MailLinkToken, Long> {
	MailLinkToken findByEmail(String email);

	void deleteByEmail(String email);
}
