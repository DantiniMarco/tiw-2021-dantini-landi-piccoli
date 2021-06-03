package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.AuctionStatus;
import it.polimi.tiw.html.beans.ExtendedAuction;
import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/SellServlet")
public class SellServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException{
        ServletContext servletContext = getServletContext();
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        User user = (User) request.getSession().getAttribute("user");
        AuctionDAO am= new AuctionDAO(connection);
        List<ExtendedAuction> openAuctions;
        List<ExtendedAuction> closedAuctions;

        try{
            openAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.OPEN);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        try{
            closedAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.CLOSED);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
        ctx.setVariable("openAuctions", openAuctions);
        ctx.setVariable("closedAuctions", closedAuctions);

        List<String> timeLeftOpen = calculateTime(openAuctions);
        List<String> timeLeftClosed = calculateTime(closedAuctions);

        ctx.setVariable("timeLeftOpen", timeLeftOpen);
        ctx.setVariable("timeLeftClosed", timeLeftClosed);
        LocalDateTime dateLowerBound = LocalDateTime.now();
        dateLowerBound = dateLowerBound.plusDays(1);
        DateTimeFormatter lowerBoundFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");
        String lowerBoundFormatted = dateLowerBound.format(lowerBoundFormatter);
        LocalDateTime dateUpperBound = LocalDateTime.now();
        dateUpperBound = dateUpperBound.plusWeeks(2);
        DateTimeFormatter upperBoundFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");
        String upperBoundFormatted = dateUpperBound.format(upperBoundFormatter);
        ctx.setVariable("dateMin", lowerBoundFormatted);
        ctx.setVariable("dateMax", upperBoundFormatted);

        String path = "/WEB-INF/Sell.html";
        templateEngine.process(path, ctx, response.getWriter());

    }

    private List<String> calculateTime(List<ExtendedAuction> auctionList) {
        List<String> timeLeftlist = new ArrayList<>();
        for (ExtendedAuction auction : auctionList) {
            long diff = auction.getDeadline().getTime() - new Date().getTime();

            if (diff <= 3600) {
                if (diff < 1) {
                    timeLeftlist.add("Expired");
                } else {
                    timeLeftlist.add("Less than an hour");
                }
            } else {
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);
                timeLeftlist.add(((diffDays > 0)?diffDays + " days and ":"")+ diffHours + " hours");
            }
        }
        return timeLeftlist;
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