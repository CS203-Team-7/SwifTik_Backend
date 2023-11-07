package com.swiftyticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.swiftyticket.services.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // We need the jwtfilter we implemented:
    private final JwtAuthFilter jwtAuthFilter;
    private final UserService userService;
    
    // This is the main function that we can use to set which parts of the controllers are accessible to which roles: Filtering user access
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider()).addFilterBefore(
                    jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/auth/**","/otp/*").permitAll();
                    request.requestMatchers("/users/**").hasAuthority("ADMIN");
                    request.requestMatchers("/events/*/open").hasAuthority("ADMIN");
                    request.requestMatchers("/events/*/close").hasAuthority("ADMIN");
                    request.requestMatchers("/events/create").hasAuthority("ADMIN");
                    request.requestMatchers("/events/{id}/createZone").hasAuthority("ADMIN");
                    request.requestMatchers("/events/{id}/raffle").hasAuthority("ADMIN")
                    .anyRequest().authenticated();
                })
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // We need a password encoder for the user's password:
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // We also need to assign an Authentication Provider from spring security:
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    // To get an auth manager as well for the filter chain to work:
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    // Cors configuration:
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(
                List.of(
                        // The links that are allowed to access the backend:
                        "http://localhost:3000",
                        "https://www.twilio.com"

                )
        );
        // Now we allow particular methods to be used:
        corsConfiguration.setAllowedMethods(
                List.of(
                        "HEAD",
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH"
                )
        );
        // Now to expose the headers to enable JWT tokens to be sent back and forth:
        corsConfiguration.setExposedHeaders(
                List.of(
                        "Access-Control-Allow-Headers",
                        "Authorization, x-xsrf-token, " +
                                "Access-Control-Allow-Headers, " +
                                "Origin, Accept, X-Requested-With, " +
                                "Content-Type, Access-Control-Request-Method, " +
                                "Access-Control-Request-Headers"
                )
        );

        // Now we allow credentials to be sent back and forth as well:
        corsConfiguration.setAllowCredentials(true);
        // Now we allow certain headers to be sent back and forth:
        corsConfiguration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Cache-Control",
                        "Content-Type"
                )
        );
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
