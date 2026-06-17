package com.mason.api.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        DiscordOAuth2UserService discordOAuth2UserService
    ) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .userInfoEndpoint(userInfo -> userInfo.userService(discordOAuth2UserService))
                .defaultSuccessUrl("/", true)
                .failureUrl("/?loginError=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );

        return http.build();
    }
}
