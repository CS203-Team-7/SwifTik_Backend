package com.swiftyticket.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftyticket.models.User;

@Repository 
public interface UserRepository  extends JpaRepository<User, Integer>{
    // We need a method that can find the user's by their email:
    Optional<User> findByEmail(String email);
}
