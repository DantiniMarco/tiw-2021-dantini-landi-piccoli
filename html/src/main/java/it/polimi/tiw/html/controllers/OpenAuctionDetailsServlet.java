package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.ExtendedAuction;
import it.polimi.tiw.html.beans.ExtendedBid;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.dao.BidDAO;
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
public class OpenAuctionDetailsServlet extends HttpServlet {
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
        String id_param = request.getParameter("auctionId");
        boolean bad_request=false;
        int id = -1;

        if(id_param==null || id_param.isEmpty()){
            bad_request=true;
        }

        try{
            id = Integer.parseInt(id_param);
        }catch(NumberFormatException ex){
            ex.printStackTrace();
            bad_request=true;
        }

        if(bad_request==true){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            throw new UnavailableException("Id parameter missing");
        }

        ExtendedAuction auction = new ExtendedAuction();
        List<ExtendedBid> bids = new ArrayList<>();
        AuctionDAO am = new AuctionDAO(con);
        BidDAO bm = new BidDAO(con);

        try{
           auction = am.findAuctionById(id);
        }catch(SQLException sqle1){
            sqle1.printStackTrace();
            throw new UnavailableException("Issue from database");
        }
        try{
            bids = bm.findBidsByIdAuction(id);
        }catch (SQLException sqle2){
            throw new UnavailableException("Issue from database");
        }
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
        ctx.setVariable("auctionData", auction);
        ctx.setVariable("bids", bids);
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
            if(con!=null){
                con.close();
            }
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }
}
