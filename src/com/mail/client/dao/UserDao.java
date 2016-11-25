package com.mail.client.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mail.client.model.UserModel;

public class UserDao {

	Connection connection;

	private String USER_BY_ID = "SELECT username, nickname, orgunit, orgname FROM vcard_search WHERE fn = ?";

	public UserDao() {
		connection = DbManager.getConnection();
	}

	public UserModel getUserById(String id) {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(USER_BY_ID);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			return map(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Map result map to usermodel
	 * 
	 * @param rs
	 * @return
	 */
	private UserModel map(ResultSet rs) {
		UserModel userModel = null;
		try {
			if (rs.next()) {
				userModel = new UserModel();
				userModel.setUserId(rs.getString("username"));
				userModel.setPhone(rs.getString("orgunit"));
				userModel.setNick(rs.getString("nickname"));
				System.out.println("found userId "+userModel.getUserId());
			} else {
				System.out.println("User detail not found");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userModel;
	}

}
