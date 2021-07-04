package com.github.diabetesassistant.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;

@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public record AccessToken(UUID userId, List<Role> roles) {}
