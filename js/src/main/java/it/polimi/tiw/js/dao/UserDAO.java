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

    /***
     * @author Alfredo Landi
     * @param id of user
     * @return firstname, lastname and home address of a user
     */
    public User getUserById(int id) throws SQLException{
        String query = "SELECT firstname, lastname, address FROM user WHERE iduser =?";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        User user = null;

        try{
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, id);
            result = pstatement.executeQuery();
            user = new User();
            while(result.next()){
                user.setFirstName(result.getString("firstname"));
                user.setLastName(result.getString("lastname"));
                user.setAddress(result.getString("address"));
            }
        }catch(SQLException sqle){
            sqle.printStackTrace();
            throw new SQLException(sqle);
        }

        return user;
    }
}
