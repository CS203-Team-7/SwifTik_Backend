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
								   "+6582887066",
								   Role.ADMIN,
								   true);

		userRepository.save(admin);
		String token = jwtService.generateToken(admin);
		log.info("admin token: " + token);

		User uzer = new User("uzer@gmail.com",
								   passwordEncoder.encode("Uzer123!"),
								   new Date(),
								   "+6582887066",
								   Role.USER,
								   true);

		userRepository.save(uzer);
		String token2 = jwtService.generateToken(uzer);
		log.info("uzer token: " + token2);

		User uzer2 = new User("uzer2@gmail.com",
									passwordEncoder.encode("Uzer123!"),
								   new Date(),
								   "+6582887066",
								   Role.USER,
								   true);

		userRepository.save(uzer2);
		String token3 = jwtService.generateToken(uzer2);
		log.info("uzer2 token: " + token3);
	}
}
