package com.mail.client.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {

	private static String host = "";
	private static String database = "";
	private static String userName = "";
	private static String password = "";
	
	private static Connection connection;
	
	public static  Connection connectToDatabase() {
		try {
			connection = DriverManager
					.getConnection("jdbc:mysql://"+host+":3306/"+database, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}
	
	public static Connection getConnection(){
		try {
			if(connection == null ||  connection.isClosed()){
			connection = connectToDatabase();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

}
