package com.scalesec.vulnado;

import java.nio.charset.StandardCharsets;
import java.sql.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import static com.scalesec.vulnado.Postgres.connection;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8); // Specify encoding
    SecretKey key = Keys.hmacShaKeyFor(secretBytes);
    String jws = Jwts.builder().setSubject(this.username).signWith(key).compact();
    return jws;
  }


  public static void assertAuth(String secret, String token) {
    try {
      byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8); // Specify encoding
      SecretKey key = Keys.hmacShaKeyFor(secretBytes);
      Jwts.parser()
              .setSigningKey(key)
              .parseClaimsJws(token);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Unauthorized(e.getMessage());
    }
  }


  public static User fetch(String un) {
    User user = null;

    try (Connection cxn = connection()) {
      System.out.println("Opened database successfully");

      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";

      try (PreparedStatement preparedStatement = cxn.prepareStatement(query)) {
        preparedStatement.setString(1, un);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
          String user_id = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(user_id, username, password);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return user;
  }


}


