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
		User admin = User.builder()
		.email("admin@gmail.com")
		.password(passwordEncoder.encode("Admin123!"))
		.dateOfBirth(new Date())
		.phoneNumber("12345678")
		.role(Role.ADMIN)
		.verified(true)
		.build();
		userRepository.save(admin);
		String token = jwtService.generateToken(admin);
		log.info(token);
	}
}
