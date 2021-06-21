package com.github.diabetesassistant.data;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends ReactiveCrudRepository<TokenEntity, UUID> {}
