package com.github.diabetesassistant.auth.domain;

import java.util.UUID;

public record IDToken(UUID userId, String email) {}
