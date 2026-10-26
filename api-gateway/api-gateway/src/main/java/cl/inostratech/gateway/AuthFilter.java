package cl.inostratech.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public AuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        System.out.println("Petición entrante: " + path);

        // 1. El login es público: se deja pasar sin token
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Debe traer el header Authorization con formato "Bearer <token>"
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Bloqueada: no trae token");
            return rechazar(exchange);
        }

        // 3. Validar firma y expiración del token
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validarToken(token);
            String usuario = claims.getSubject();
            System.out.println("Permitida para el usuario: " + usuario);

            // 4. Le pasamos el usuario al microservicio en un header
            ServerWebExchange exchangeConUsuario = exchange.mutate()
                    .request(r -> r.header("X-Usuario", usuario))
                    .build();

            return chain.filter(exchangeConUsuario);

        } catch (JwtException e) {
            System.out.println("Bloqueada: token inválido o vencido");
            return rechazar(exchange);
        }
    }

    private Mono<Void> rechazar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}