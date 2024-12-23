package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {

	private final String USERNAME = "root";
	private final String PASSWORD = "";
	private final String DATABASE = "stellarfest";
	private final String HOST = "localhost";

	private final String CONNECTION = String.format("jdbc:mysql://%s/%s", HOST, DATABASE);

	public ResultSet rs;
	public ResultSetMetaData rsm;

	private Connection con;
	private Statement st;

	private Connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
			st = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Connect connect = null;

	public static Connect getInstance() {
		if (connect == null)
			connect = new Connect();
		return connect;
	}

	// SELECT
	public ResultSet execQuery(String Query) {
		try {
			rs = st.executeQuery(Query);
			rsm = rs.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rs;
	}

	// INSERT, UPDATE, DELETE (Buat jalanin Query SQL)
	public void execUpdate(String Query) {
		try {
			st.executeUpdate(Query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PreparedStatement preparedStatement(String query) {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ps;
	}
}
