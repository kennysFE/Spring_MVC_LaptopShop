package vn.hoidanit.laptopshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import jakarta.servlet.DispatcherType;
import vn.hoidanit.laptopshop.service.CustomUserDetailsService;
import vn.hoidanit.laptopshop.service.UserService;
import vn.hoidanit.laptopshop.service.userinfo.CustomOAuth2UserService;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UserService userService) {
                return new CustomUserDetailsService(userService);
        }

        /**
         * 
         * @param passwordEncoder:    BCryptPasswordEncoder
         * @param userDetailsService: CustomUserDetailsService
         * @return This method configures the authProvider to use the userDetailsService
         *         and passwordEncoder that it defined in the previous beans.
         */
        @Bean
        public DaoAuthenticationProvider authProvider(
                        PasswordEncoder passwordEncoder,
                        UserDetailsService userDetailsService) {

                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);
                // authProvider.setHideUserNotFoundExceptions(false);

                return authProvider;
        }

        /**
         * 
         * @return CustomSuccessHandle has been defined {@link CustomSuccessHandle}
         */
        @Bean
        public AuthenticationSuccessHandler customSuccessHandler() {
                return new CustomSuccessHandle();
        }

        /**
         * 
         * @return Provides a "Remember Me" mechanism, helping users stay logged in
         *         after closing the browser
         */
        @Bean
        public SpringSessionRememberMeServices rememberMeServices() {
                SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
                // optionally customize
                rememberMeServices.setAlwaysRemember(true);
                return rememberMeServices;
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                                                DispatcherType.INCLUDE)
                                                .permitAll()

                                                .requestMatchers("/", "/login", "/client/**", "/product/**",
                                                                "/products/**", "/register",
                                                                "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                // Login Google
                                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                                                .successHandler(customSuccessHandler())
                                                .failureUrl("/login?error")
                                                .userInfoEndpoint(user -> user
                                                                .userService(new CustomOAuth2UserService(userService))))

                                // Session management
                                .sessionManagement((sessionManagement) -> sessionManagement
                                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                                .invalidSessionUrl("/logout?expired")
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))

                                // logout -> invalid session
                                .logout(logout -> logout.deleteCookies("JSESSIONID").invalidateHttpSession(true))

                                .rememberMe(r -> r.rememberMeServices(rememberMeServices()))

                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .failureUrl("/login?error")
                                                .successHandler(customSuccessHandler())
                                                .permitAll())

                                .exceptionHandling(ex -> ex.accessDeniedPage("/access-deny"));

                return http.build();
        }

}
