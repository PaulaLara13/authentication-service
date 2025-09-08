package co.com.pragma.model.user.gateways;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigInteger;

public interface RoleRpository {
    Mono findById(BigInteger id);
    Mono findByName(String name);
    Flux findAll();
}
