package com.github.diabetesassistant.user.domain;

import java.util.Optional;
import java.util.UUID;

public record User(Optional<UUID> id, String email, String password, Role role) {}
