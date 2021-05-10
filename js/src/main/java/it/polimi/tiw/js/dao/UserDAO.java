package it.polimi.tiw.js.dao;

import it.polimi.tiw.js.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection con;

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    public User checkCredentials(String usrn, String pwd) throws SQLException {
        String query = "SELECT  iduser, username, firstname, lastname FROM user WHERE username = ? AND password =?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, usrn);
            pstatement.setString(2, pwd);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    result.next();
                    User user = new User();
                    user.setIdUser(result.getInt("iduser"));
                    user.setUsername(result.getString("username"));
                    user.setFirstName(result.getString("firstname"));
                    user.setLastName(result.getString("lastname"));
                    return user;
                }
            }
        }
    }
}
