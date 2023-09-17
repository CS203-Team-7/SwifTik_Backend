package com.swiftyticket.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.swiftyticket.models.User;

import jakarta.transaction.Transactional;

@Repository 
public interface UserRepository  extends JpaRepository<User, Integer>{
    // We need a method that can find the user's by their email:
    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User a " +
            "SET a.verified = TRUE WHERE a.email = ?1")
    int enableAppUser(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User a WHERE a.email = ?1")
    void deleteByEmail(String email);
}
