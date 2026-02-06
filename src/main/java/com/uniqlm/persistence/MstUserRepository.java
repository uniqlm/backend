package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MstUserRepository extends JpaRepository<MstUser, Integer> {

    Optional<MstUser> findByUsername(String username);

    // Added to support looking up users by email (useful for Forgot Password & OAuth)
    Optional<MstUser> findByEmail(String email);

    Optional<MstUser> findByResetToken(String resetToken);

    Optional<MstUser> findByVerificationToken(String verificationToken);
}