package com.swiftyticket;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SwiftyticketingApplication implements CommandLineRunner{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	public static void main(String[] args) {
		SpringApplication.run(SwiftyticketingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		User admin = new User("admin@gmail.com",
								   passwordEncoder.encode("Admin123!"),
								   new Date(),
								   "12345678",
								   Role.ADMIN,
								   true);

		userRepository.save(admin);
		String token = jwtService.generateToken(admin);
		log.info("admin token: " + token);
	}
}
