package com.smartbook.repository;

import com.smartbook.entity.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository
        extends JpaRepository<AccountToken, Long> {

    Optional<AccountToken> findByTokenHashAndTokenTypeAndUsedFalse(
            String tokenHash,
            String tokenType
    );
}