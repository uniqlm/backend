package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MstUserDaoService {

    private final MstUserRepository mstUserRepository;

    // Standard lookup
    public MstUser getByUsername(String username) {
        return mstUserRepository.findByUsername(username).orElse(null);
    }

    // New: Support email-based lookup for OAuth/Forgot Password
    public MstUser getByEmail(String email) {
        return mstUserRepository.findByEmail(email).orElse(null);
    }

    public MstUser getById(Integer id) {
        return mstUserRepository.findById(id).orElse(null);
    }

    public List<MstUser> getAllUsers() {
        return mstUserRepository.findAll();
    }

    @Transactional
    public MstUser saveUser(MstUser user) {
        // This handles both create and update
        return mstUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        mstUserRepository.deleteById(id);
    }

    // Token Lookups
    public MstUser getByResetToken(String token) {
        return mstUserRepository.findByResetToken(token).orElse(null);
    }

    public MstUser getByVerificationToken(String token) {
        return mstUserRepository.findByVerificationToken(token).orElse(null);
    }

    // Helper for Registration
    public boolean existsByUsername(String username) {
        return mstUserRepository.findByUsername(username).isPresent();
    }
}