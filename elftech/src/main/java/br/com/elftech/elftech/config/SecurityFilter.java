package br.com.elftech.elftech.config;

import br.com.elftech.elftech.repository.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Value("${api.security.token.secret}")
    private String secret;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Ignora a validação de token para requisições OPTIONS do CORS
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            try {
                Algorithm algoritmo = Algorithm.HMAC256(secret);
                var subject = JWT.require(algoritmo)
                        .withIssuer("API Restaurante SaaS")
                        .build()
                        .verify(tokenJWT)
                        .getSubject();

                var usuario = usuarioRepository.findByLogin(subject)
                        .orElseThrow(() -> new RuntimeException("Usuário do token não encontrado no banco"));

                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JWTVerificationException exception) {
                // Apenas loga o erro e deixa passar (sem autenticação)
                System.err.println("Token JWT inválido ou expirado: " + exception.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}