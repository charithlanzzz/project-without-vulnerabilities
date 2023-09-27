package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  public String id, username, body;
  public Timestamp created_on;

  public Comment(String id, String username, String body, Timestamp created_on) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.created_on = created_on;
  }

  public static Comment create(String username, String body){
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try {
      if (comment.commit()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (Exception e) {
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetch_all() {
    List<Comment> comments = new ArrayList<>();

    try (Connection cxn = Postgres.connection();
         Statement stmt = cxn.createStatement()) {

      String query = "SELECT * FROM comments;";
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp created_on = rs.getTimestamp("created_on");
        Comment c = new Comment(id, username, body, created_on);
        comments.add(c);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }

    return comments;
  }


  public static boolean delete(String id) {
    String sql = "DELETE FROM comments WHERE id = ?";

    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement(sql)) {

      pStatement.setString(1, id);
      int rowsAffected = pStatement.executeUpdate();
      return rowsAffected == 1;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false; // Return false in case of exceptions or no rows affected
  }


  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";

    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement(sql)) {

      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.created_on);

      int rowsAffected = pStatement.executeUpdate();
      return rowsAffected == 1;
    }
  }

}
