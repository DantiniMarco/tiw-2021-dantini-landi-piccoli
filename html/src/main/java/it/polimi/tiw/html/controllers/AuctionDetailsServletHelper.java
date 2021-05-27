package it.polimi.tiw.html.controllers;

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

@WebServlet("/AuctionDetailsServletHelper")
public class AuctionDetailsServletHelper extends HttpServlet {
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
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        AuctionDAO am = new AuctionDAO(con);
        String id_param = request.getParameter("auctionId");
        boolean bad_request = false;
        int id = -1;

        if(id_param==null || id_param.isEmpty()){
            bad_request = true;
        }

        try{
            id = Integer.parseInt(id_param);
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            bad_request = true;
        }

        if(bad_request==true){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter");
            throw new UnavailableException("Missing parameter");
        }

        try{
            am.closeAuction(id);
        }catch (SQLException sqle){
            sqle.printStackTrace();
            throw new UnavailableException("Issue from database");
        }

        String path = "AuctionDetailsServlet?auctionId=" + id;
        System.out.println(path);
        response.sendRedirect(path);

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
