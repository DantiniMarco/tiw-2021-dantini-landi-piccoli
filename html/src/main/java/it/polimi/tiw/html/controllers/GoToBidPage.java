package it.polimi.tiw.html.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.html.beans.*;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.dao.BidDAO;
import it.polimi.tiw.html.dao.ItemDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/GoToBidPage")
public class GoToBidPage extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public GoToBidPage() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idAuction;
        Item item;
        List<ExtendedBid> bids;
        float currMaxPrice;
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        BidDAO bidDAO = new BidDAO(connection);
        float minRaise=-1;
        List<Integer> idList = new ArrayList<>();
        AuctionDAO auctionDAO = new AuctionDAO(connection);
        User user = (User) request.getSession().getAttribute("user");


        if (request.getParameter("error") != null && request.getParameter("error").equals("lowPrice")) {
            ctx.setVariable("errorMsg", "This price is too low. You may insert an offer higher than the current max.");
        }
        if (request.getParameter("error") != null && request.getParameter("error").equals("wrongFormat")) {
            ctx.setVariable("errorMsg", "Input is not correctly formatted (prices are written e.g. 20.05)");
        }

        try {
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "incorrect param values");
            return;
        }
        try{
            idList = auctionDAO.findLegitIdsBid(user.getIdUser());
        } catch (SQLException throwables) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            return;
        }
        if(!idList.contains(idAuction)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "incorrect request");
            return;
        }
        try {
            minRaise = bidDAO.findMinRaise(idAuction);
            if(minRaise >=0)
                ctx.setVariable("minimumRaise", minRaise);
        } catch (SQLException throwables) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            return;
        }

        try {
            currMaxPrice = bidDAO.findPriceForNewBid(idAuction);
            ctx.setVariable("currMax", currMaxPrice);
        } catch (SQLException sqle) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            return;
        }


        try {
            ItemDAO itemDAO = new ItemDAO(connection);
            item = itemDAO.getItemById(idAuction);
            if (item == null) {
                ctx.setVariable("errorMsg", "No info for this article found.");
            } else {
                ctx.setVariable("item", item);
            }
        } catch (SQLException e) {
            response.sendError(500, "Database access failed");
            return;
        }
        try {
            bids = bidDAO.findBidsByIdAuction(idAuction);
            if(bids!= null){
                ctx.setVariable("bids", bids);
            }
            ctx.setVariable("idauction", idAuction);
        } catch (SQLException e) {
            response.sendError(500, "Database access failed");
            return;
        }

        String path = "/WEB-INF/GoToBidPage.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
