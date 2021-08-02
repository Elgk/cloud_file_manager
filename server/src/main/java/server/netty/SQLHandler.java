package server.netty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement resultSet;
    private static PreparedStatement usersSet;
    private static PreparedStatement foldersSet;

    public static boolean connect()  {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:cloud_storage.db");
            prepareAllStatements();
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() throws SQLException {
        resultSet.close();
        usersSet.close();
        foldersSet.close();
        connection.close();
    }

    private static void prepareAllStatements(){
        try {
            resultSet = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            usersSet = connection.prepareStatement("SELECT nickname, id FROM users WHERE login = ? AND password = ?; ");
            foldersSet = connection.prepareStatement("SELECT name FROM network_folders WHERE server_name = ? AND user_id = ? order by name");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static String[] getNicknameByLoginPassword(String login, String password) {
        try {
                String[] resultData = new String[2];
                usersSet.setString(1, login);
                usersSet.setString(2, password);
                ResultSet rs = usersSet.executeQuery();

                if (rs.next()) {
                    resultData[0] = rs.getString(1);
                    resultData[1] = rs.getString(2);
                    return resultData;
                } else {
                    return null;
                }

            } catch(SQLException throwables){
                throwables.printStackTrace();
                return null;
            }

    }
    public static List<String> getUserFolder(String server_name, String user_id){
        List<String> folderList = new ArrayList<>();
        try {
            foldersSet.setString(1, server_name);
            foldersSet.setString(2, user_id);
            ResultSet rs = foldersSet.executeQuery();
            while (rs.next()){
                folderList.add(rs.getString(1));
            }
            return folderList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return folderList;
        }
    }

}
