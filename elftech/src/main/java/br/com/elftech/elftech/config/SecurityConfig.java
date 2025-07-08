package br.com.elftech.elftech.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    // ---- REGRAS PARA ROTAS PROTEGIDAS ----
                    req.requestMatchers(HttpMethod.POST, "/api/restaurantes/**").authenticated();
                    req.requestMatchers(HttpMethod.PUT, "/api/restaurantes/**").authenticated();
                    req.requestMatchers(HttpMethod.DELETE, "/api/restaurantes/**").authenticated();

                    // Apenas usuários autenticados podem ver ou deletar outros usuários
                    req.requestMatchers(HttpMethod.GET, "/api/usuarios/**").authenticated();
                    req.requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").authenticated();


                    // ---- REGRAS PARA ROTAS PÚBLICAS ----
                    // O endpoint de login e registro devem ser acessíveis publicamente
                    req.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registrar", "/api/restaurantes", "/api/public/pedidos").permitAll();
                    req.requestMatchers("/api/public/**").permitAll();

                    // Se a intenção é que todas as outras rotas sejam protegidas por padrão, use authenticated()
                    // Caso contrário, se outras rotas devem ser públicas, adicione-as explicitamente.
                    // Evite anyRequest().permitAll() a menos que você queira um backend totalmente público.
                    req.anyRequest().authenticated(); // Alterado para exigir autenticação por padrão
                })
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
