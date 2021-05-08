package it.polimi.tiw.html.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.AuctionDAO;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.html.beans.Auction;

@WebServlet("/GetSearchedAuction")
public class GetSearchedAuction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public GetSearchedAuction(){super();}

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
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
        //check parameter is present
        /*if( keyWord == null ){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameters");
            return;
        }*/
            User u = (User) s.getAttribute("user");
        AuctionDAO aDAO = new AuctionDAO(connection);
        ArrayList<Auction> searchedList;
            //try{
            //searchedList = aDAO.findOpenAuction(keyWord);
            /*
            if(searchedList.isEmpty()){
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
                ctx.setVariable("errorMsg", "This keyword is not matching to any open auction");
                String path = "/index.html";
                templateEngine.process(path, ctx, response.getWriter());

            } else{*/
            String path = "/WEB-INF/GetSearchedAuction.html";
            //request.setAttribute("searchedList", searchedList);
            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            dispatcher.forward(request,response);
            //}
        //} catch(SQLException e){
             //   response.sendError(500, "Database access failed");
           // }
    }
    public void destroy(){
        try{
            if(connection != null){
                connection.close();
            }
        }catch (SQLException sqle){}
    }

}

