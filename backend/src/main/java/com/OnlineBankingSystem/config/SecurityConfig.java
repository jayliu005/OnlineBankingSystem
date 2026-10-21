package com.OnlineBankingSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.OnlineBankingSystem.user.BankUserRepository;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
						(request, response, exception) ->
								response.sendError(HttpStatus.UNAUTHORIZED.value())))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
						.anyRequest().authenticated())
				.build();
	}

	@Bean
	UserDetailsService userDetailsService(BankUserRepository repository) {
		return userName -> repository.findByUserName(userName)
				.map(bankUser -> User.withUsername(bankUser.getUserName())
						.password(bankUser.getPassword())
						.roles("USER")
						.build())
				.orElseThrow(() -> new UsernameNotFoundException(
						"No such user with userName '%s'".formatted(userName)));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
			throws Exception {
		return configuration.getAuthenticationManager();
	}
}
