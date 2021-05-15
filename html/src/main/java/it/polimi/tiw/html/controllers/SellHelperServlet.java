package it.polimi.tiw.html.controllers;

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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;

@WebServlet("/SellHelperServlet")
public class SellHelperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;
    private TemplateEngine templateEngine;

    public void init() throws ServletException{
        ServletContext servletContext = getServletContext();
        con = ConnectionHandler.getConnection(getServletContext());
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String itemName = request.getParameter("itemName");
        String itemImage = request.getParameter("itemImage");
        String itemDescription = request.getParameter("itemDescription");
        String initialPrice_param = request.getParameter("initialPrice");
        String minRaise_param = request.getParameter("minRaise");
        String deadline_param = request.getParameter("deadline");
        int bad_request=0;
        float initialPrice=0;
        float minRaise =0;
        Date deadline= null;
        AuctionDAO am = new AuctionDAO(con);

        if(itemName==null || itemName.isEmpty() || itemImage==null || itemImage.isEmpty() || itemDescription==null || itemDescription.isEmpty()
        || initialPrice_param==null || initialPrice_param.isEmpty() || minRaise_param==null || minRaise_param.isEmpty() || deadline_param==null
        || deadline_param.isEmpty()){
            bad_request=1;
        }

        try{
            initialPrice= Float.parseFloat(initialPrice_param);
            minRaise = Float.parseFloat(minRaise_param);
            deadline = Date.valueOf(deadline_param);
        }catch(NumberFormatException e){
            bad_request=1;
        }catch(Exception e){
            bad_request=1;
        }

        if(bad_request==1){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter/s missing");
        }

        //Forse andrebbe controllato (?)
        User user = (User) request.getSession().getAttribute("user");
        System.out.println(user);
        /*try{
            am.insertNewAuction(itemName,itemImage, itemDescription, initialPrice, minRaise, deadline, idCreator);
        }catch (SQLException sqle){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
        }*/

        HttpSession s = request.getSession();
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
        String path = "/WEB-INF/Sell.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }


    @Override
    public void destroy() {
        try{
            if(con!=null){
                con.close();
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
    }
}
