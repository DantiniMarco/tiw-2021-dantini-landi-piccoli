package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.polimi.tiw.js.beans.Auction;
import it.polimi.tiw.js.beans.ExtendedAuction;
import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.dao.BidDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GetSearchedAuction")
public class GetSearchedAuction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public GetSearchedAuction() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession s = request.getSession();
        String keyWord = request.getParameter("keyword");
        ServletContext servletContext = getServletContext();
        List<ExtendedAuction> searchedList = null;
        User user = (User) request.getSession().getAttribute("user");
        int idBidder = user.getIdUser();
        BidDAO bidDAO = new BidDAO(connection);


        if (keyWord != null) {
            if (keyWord.length() < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Keyword is too short");
                return;
            } else {
                AuctionDAO aDAO = new AuctionDAO(connection);

                try {
                    searchedList = aDAO.findOpenAuction(keyWord, idBidder);
                    if (searchedList == null || searchedList.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println("Keyword does not match any resource");
                        return;
                    }
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Database access failed");
                    return;
                }
            }
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                if(value != null) {
                    out.value(value.format(DateTimeFormatter.ISO_INSTANT));
                }else {
                    out.value("");
                }
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }
        })
        .create();
        String json = gson.toJson(searchedList);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);


    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

