package co.com.pragma.api.security;

import co.com.pragma.api.security.jwt.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationWebFilter(JwtTokenProvider tokenProvider) {
    super(
        (ReactiveAuthenticationManager)
            authentication -> {
              if (authentication instanceof UsernamePasswordAuthenticationToken) {
                return Mono.just(authentication);
              }
              return Mono.error(
                  new UnsupportedOperationException("Unsupported authentication type"));
            });
    this.tokenProvider = tokenProvider;
    setServerAuthenticationConverter(createJwtConverter());
  }

  private ServerAuthenticationConverter createJwtConverter() {
    return exchange -> {
      return Mono.justOrEmpty(exchange)
          .flatMap(this::extractJwtFromRequest)
          .filter(token -> tokenProvider.validateToken(token))
          .flatMap(
              token -> {
                String username = tokenProvider.getUsernameFromToken(token);
                List<String> roles = tokenProvider.getRolesFromToken(token);

                List<SimpleGrantedAuthority> authorities =
                    roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                Authentication auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                return Mono.just(auth);
              });
    };
  }

  private Mono<String> extractJwtFromRequest(ServerWebExchange exchange) {
    return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
        .filter(authHeader -> authHeader.startsWith("Bearer "))
        .map(authHeader -> authHeader.substring(7));
  }
}
