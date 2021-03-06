package server.netty;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement resultSet;
    private static PreparedStatement usersSet;
    private static PreparedStatement foldersSet;
    private static PreparedStatement resultFoldersSet;

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
        resultFoldersSet.close();
        usersSet.close();
        foldersSet.close();
        connection.close();
    }

    private static void prepareAllStatements(){
        try {
            resultSet = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            usersSet = connection.prepareStatement("SELECT nickname, id FROM users WHERE login = ? AND password = ?; ");
            foldersSet = connection.prepareStatement("SELECT name FROM network_folders WHERE server_name = ? AND user_id = ?");
            resultFoldersSet = connection.prepareStatement("SELECT name FROM network_folders WHERE server_name = ? AND user_id = ?");

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
    public static String getUserFolder(String server_name, String user_id){

        try {
            foldersSet.setString(1, server_name);
            foldersSet.setString(2, user_id);
            ResultSet rs = foldersSet.executeQuery();
            while (rs.next()){
               return rs.getString(1);
            }
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

}
