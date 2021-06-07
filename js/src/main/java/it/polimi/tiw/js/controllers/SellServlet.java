package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.polimi.tiw.js.beans.AuctionStatus;
import it.polimi.tiw.js.beans.ExtendedAuction;
import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@WebServlet("/SellServlet")
public class SellServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException{
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        User user = (User) request.getSession().getAttribute("user");
        AuctionDAO am= new AuctionDAO(connection);
        List<ExtendedAuction> openAuctions;
        List<ExtendedAuction> closedAuctions;
        HashMap<String, Object> sellMap = new HashMap<>();

        try{
            openAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.OPEN);
            sellMap.put("openAuctions", openAuctions);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        try{
            closedAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.CLOSED);
            sellMap.put("closedAuctions", closedAuctions);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        ZonedDateTime dateLowerBound = ZonedDateTime.now(ZoneOffset.UTC);
        dateLowerBound = dateLowerBound.plusDays(1);
        sellMap.put("dateLowerBound", dateLowerBound.format(DateTimeFormatter.ISO_INSTANT));
        ZonedDateTime dateUpperBound = ZonedDateTime.now(ZoneOffset.UTC);
        dateUpperBound = dateUpperBound.plusWeeks(2);
        sellMap.put("dateUpperBound", dateUpperBound.format(DateTimeFormatter.ISO_INSTANT));

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

        String json = gson.toJson(sellMap);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy(){
        try{
            ConnectionHandler.closeConnection(connection);
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }
}