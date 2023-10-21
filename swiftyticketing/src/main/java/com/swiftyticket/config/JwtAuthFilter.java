package com.swiftyticket.config;

import java.io.IOException;
import java.util.logging.Logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.swiftyticket.exceptions.JWTExpiredException;
import com.swiftyticket.services.JwtService;
import com.swiftyticket.services.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    // We need another logger here:
    private final Logger logger = Logger.getLogger(JwtAuthFilter.class.getName());

    // Below is the filter chain we make that uses jwt authentication to allow users or not:
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // We need to get the token from the request header and validate it:
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            logger.warning("No Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            logger.warning("Invalid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        // We need to extract the token from the header and validate it:
        final String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            logger.warning("Invalid token found");
            filterChain.doFilter(request, response);
            return;
        }

        // Get user details from the token:
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(jwtService.extractUserName(token));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails == null ? null : userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // We need to set the security context to the authentication:
        logger.info("Auth OK");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
