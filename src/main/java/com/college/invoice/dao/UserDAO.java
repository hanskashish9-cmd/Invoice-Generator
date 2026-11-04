package com.college.invoice.dao;

import com.college.invoice.model.User;
import com.college.invoice.util.DBConnection;
import com.college.invoice.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /** Returns the User if username and password are correct, otherwise null. */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, full_name FROM users "
                   + "WHERE username = ? AND password_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"),
                                    rs.getString("username"),
                                    rs.getString("full_name"));
                }
            }
        }
        return null;
    }
}