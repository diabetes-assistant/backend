package com.github.diabetesassistant.auth.data;

import static com.github.diabetesassistant.auth.data.TokenTypeEntity.ACCESS_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.diabetesassistant.DatabaseTest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TokenRepositoryTest extends DatabaseTest {
  @Autowired private TokenRepository tokenRepository;

  @Test
  void shouldCreateAccessToken() throws SQLException {
    UUID userId = UUID.randomUUID();
    LocalDateTime now = LocalDate.now().atStartOfDay();
    TokenEntity tokenEntity = new TokenEntity(ACCESS_TOKEN, userId, now);

    TokenEntity actual = this.tokenRepository.save(tokenEntity).block();
    TokenEntity expected = this.findToken(userId.toString());

    assertEquals(expected, actual);
  }

  private TokenEntity findToken(String userId) throws SQLException {
    Statement statement = this.connection.createStatement();
    String sql = "SELECT * FROM tokens WHERE type = 'ACCESS_TOKEN' AND user_id = '" + userId + "'";
    ResultSet resultSet = statement.executeQuery(sql);
    if (resultSet.next()) {
      UUID id = UUID.fromString(resultSet.getString("id"));
      TokenTypeEntity type = TokenTypeEntity.valueOf(resultSet.getString("type"));
      UUID createdUserId = UUID.fromString(resultSet.getString("user_id"));
      LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
      return new TokenEntity(id, type, createdUserId, createdAt);
    }
    return null;
  }
}
