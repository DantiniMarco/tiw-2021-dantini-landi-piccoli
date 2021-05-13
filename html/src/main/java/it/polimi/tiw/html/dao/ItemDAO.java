package it.polimi.tiw.html.dao;

import it.polimi.tiw.html.beans.Item;

import java.sql.*;

public class ItemDAO {
    private Connection con;

    /***
     * @author Alfredo Landi
     * Costructor for this DAO
     * @param con is the current connection
     */
    public ItemDAO(Connection con){
        this.con=con;
    }

    /**
     * @author Alfredo Landi
     * @param newItem to add database
     * @return the id of item if query is successful, 0 in case of db error
     */
    public int insertNewItem(Item newItem){
        ResultSet result;
        String query = "INSERT INTO item (name, image, description) VALUES (?, ?, ?)";
        PreparedStatement pstatement = null;

        try{
            pstatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstatement.setString(1, newItem.getName());
            pstatement.setString(2, newItem.getImage());
            pstatement.setString(3, newItem.getDescription());
            //Qui c'è un problema serio, l'id dell'item non può essere scelto dal db
            int affectedRows = pstatement.executeUpdate();
            if(affectedRows == 0){
                // Create item failed
                return -1;
            }
            result = pstatement.getGeneratedKeys();
            if(result != null && result.next()) {
                return result.getInt(1);
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }

        return -1;
    }
}

