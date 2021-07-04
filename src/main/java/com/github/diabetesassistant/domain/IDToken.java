package com.github.diabetesassistant.domain;

import java.util.UUID;

public record IDToken(UUID userId, String email) {}
