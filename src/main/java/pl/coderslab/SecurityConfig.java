package pl.coderslab;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import pl.coderslab.users.CustomUserDetailsService;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/users/add") // ← Wyłącz CSRF dla tego endpointu
//                )
                //TO WYRZUCIC ZEBY PONOWNIE DZIAŁAŁO LOGOWANIR
                .authorizeHttpRequests(authz -> authz
                                .anyRequest().permitAll() // Wszystkie endpointy publiczne
                )
                .csrf(csrf -> csrf.disable()) // Wyłącz CSRF dla testów API
                .formLogin(form -> form.disable()) // Wyłącz formularz logowania
                .httpBasic(basic -> basic.disable()); //Wylacz Basic Auth


//                        .requestMatchers("/", "/login", "/register", "/events/public/**", "/ui/events", "/users/add").permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/organizer/**").hasRole("ORGANIZER")
//                        .requestMatchers("/participant/**").hasRole("PARTICIPANT")
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .usernameParameter("email") // ← Tell Spring to use 'email' parameter
//                        .passwordParameter("password")
//                        .successHandler(customAuthenticationSuccessHandler())
//                        .failureUrl("/login?error=true")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/")
//                        .permitAll()
//                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            protected String determineTargetUrl(HttpServletRequest request,
                                                HttpServletResponse response, Authentication authentication) {

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                for (GrantedAuthority authority : authorities) {
                    String role = authority.getAuthority();

                    switch (role) {
                        case "ROLE_ADMIN":
                            return "/admin/dashboard";
                        case "ROLE_ORGANIZER":
                            return "/organizer/dashboard";
                        case "ROLE_PARTICIPANT":
                            return "/participant/events";
                    }
                }
                return "";
            }
        };
    }

}