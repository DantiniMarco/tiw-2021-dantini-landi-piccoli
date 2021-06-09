package it.polimi.tiw.html.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.html.beans.ExtendedAuction;
import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.dao.BidDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/GetSearchedAuction")
public class GetSearchedAuction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public GetSearchedAuction() {
        super();
    }

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession s = request.getSession();
        String keyWord = request.getParameter("keyword");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        User user = (User) request.getSession().getAttribute("user");
        int idBidder = user.getIdUser();

        BidDAO bidDAO = new BidDAO(connection);
        List<ExtendedAuction> wonList;
        try {
            wonList = bidDAO.findWonBids(idBidder);
            if (wonList == null || wonList.isEmpty()) {
                ctx.setVariable("errorMsg", "You haven't won an auction yet");
            } else {
                ctx.setVariable("wonList", wonList);
            }
        } catch (SQLException e) {
            response.sendError(500, "Database access failed");
        }
        if (keyWord != null) {
            if (keyWord.length() < 3) {
                ctx.setVariable("errorMsgForm", "Try again, this keyword is too short.");
            } else {
                AuctionDAO aDAO = new AuctionDAO(connection);
                List<ExtendedAuction> searchedList;
                try {
                    searchedList = aDAO.findOpenAuction(keyWord, idBidder);

                    if (searchedList == null || searchedList.isEmpty()) {
                        ctx.setVariable("errorMsgForm", "This keyword doesn't match any open auction.");
                    } else {
                        ctx.setVariable("auctions", searchedList);
                    }
                } catch (SQLException e) {
                    response.sendError(500, "Database access failed");
                }
            }
        }
        String path = "/WEB-INF/GetSearchedAuction.html";
        templateEngine.process(path, ctx, response.getWriter());

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

