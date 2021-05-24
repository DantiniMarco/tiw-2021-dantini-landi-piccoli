package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.js.beans.Auction;
import it.polimi.tiw.js.dao.AuctionDAO;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/GetSearchedAuction")
public class GetSearchedAuction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetSearchedAuction(){super();}

    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }
    }
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        HttpSession s = request.getSession();
        String keyWord = request.getParameter("keyword");
        ServletContext servletContext = getServletContext();
        Map<Auction, String> searchedList = null;

        if( keyWord != null ){
            if(keyWord.length()<3){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Keyword is too short");
                return;
            }
            else{
            AuctionDAO aDAO = new AuctionDAO(connection);

            try{
                searchedList = aDAO.findOpenAuction(keyWord);
                if(searchedList == null || searchedList.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println("Keyword does not match any resource");
                    return;
                }
            } catch(SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database access failed");
                return;
            }
            }
        }
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy MMM dd").create();
        String json = gson.toJson(searchedList);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

    }
    public void destroy(){
        try{
            if(connection != null){
                connection.close();
            }
        }catch (SQLException sqle){}
    }

}

