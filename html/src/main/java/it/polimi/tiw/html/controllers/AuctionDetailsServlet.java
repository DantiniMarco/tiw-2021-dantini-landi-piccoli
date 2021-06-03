package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.*;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.dao.BidDAO;
import it.polimi.tiw.html.dao.UserDAO;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/AuctionDetailsServlet")
public class AuctionDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        con = ConnectionHandler.getConnection(getServletContext());
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuctionDAO am = new AuctionDAO(con);
        String id_param = request.getParameter("auctionId");
        boolean bad_request=false;
        int id = -1;
        int winnerId = -1;
        User winner = null;
        List<ExtendedBid> bids = null;

        if(id_param==null || id_param.isEmpty()){
            bad_request=true;
        }

        try{
            id = Integer.parseInt(id_param);
        }catch(NumberFormatException ex){
            ex.printStackTrace();
            bad_request=true;
        }

        User user = (User) request.getSession().getAttribute("user");
        List<Integer> ids;
        try{
            ids = am.findAuctionIdsByUsernameId(user.getIdUser());
        }catch(SQLException sqle){
            sqle.printStackTrace();
            throw new UnavailableException("Issue from database");
        }

        if(!ids.contains(id)){
            bad_request = true;
        }

        if(bad_request==true){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            throw new UnavailableException("Id parameter missing");
        }

        ExtendedAuction auction;
        BidDAO bm = new BidDAO(con);
        UserDAO um = new UserDAO(con);

        try{
           auction = am.findAuctionById(id);
        }catch(SQLException sqle1){
            sqle1.printStackTrace();
            throw new UnavailableException("Issue from database");
        }
        if(auction.getStatus().getValue() == AuctionStatus.OPEN.getValue()){
            try{
                bids = bm.findBidsByIdAuction(id);
            }catch (SQLException sqle2){
                throw new UnavailableException("Issue from database");
            }
        }else{
            try{
                con.setAutoCommit(false);
                try{
                    winnerId = bm.findWinnerIdByAuctionId(id);
                    if(winnerId>0){
                        winner = um.getUserById(winnerId);
                    }
                }catch (SQLException sqle1){
                    con.rollback();
                    throw new UnavailableException("Issue from database");
                }finally{
                    con.setAutoCommit(true);
                }
            }catch (SQLException sqle2){
                sqle2.printStackTrace();
                throw new UnavailableException("Issue form database");
            }
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
        ctx.setVariable("auctionData", auction);
        ctx.setVariable("bids", bids);
        ctx.setVariable("winner", winner);

        String errorMsg = request.getParameter("errorMsg");
        System.out.println(errorMsg);
        if(errorMsg!=null){
            System.out.println(errorMsg.equals("wrongClosure"));
            if(errorMsg.equals("wrongClosure")){
                ctx.setVariable("errorMsg", errorMsg);
            }
        }

        String path = "/WEB-INF/AuctionDetails.html";
        templateEngine.process(path, ctx, response.getWriter());

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy(){
        try{
            ConnectionHandler.closeConnection(con);
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }

}