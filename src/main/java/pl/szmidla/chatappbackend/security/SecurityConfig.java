package pl.szmidla.chatappbackend.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import pl.szmidla.chatappbackend.config.PropertiesConfig;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
                                                   PropertiesConfig propertiesConfig, UserDetailsServiceImpl userDetailsService) throws Exception {
        JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter(authenticationManager, propertiesConfig);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");

        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();

        http.csrf().disable();
        http.cors().configurationSource( request -> corsConfiguration );
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.authorizeHttpRequests( requests -> requests
                .antMatchers("/api/login/**").permitAll()
                .antMatchers("/api/register/**").permitAll()
                .anyRequest().authenticated());
        http.addFilter(jwtAuthenticationFilter);
        http.addFilterBefore(new MyAuthorizationFilter(userDetailsService, propertiesConfig), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
