package it.polimi.tiw.html.controllers;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.UserDAO;
import org.apache.commons.text.StringEscapeUtils;
import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public ImageServlet() {
        super();
    }
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/jpeg");
        System.out.println(request.getParameter("name"));
        ServletOutputStream out;
        out = response.getOutputStream();
        FileInputStream flinp = new FileInputStream(System.getProperty("catalina.home") + File.separator + "img" + File.separator
                + request.getParameter("name"));
        BufferedInputStream buffinp = new BufferedInputStream(flinp);
        BufferedOutputStream buffoup = new BufferedOutputStream(out);
        int ch=0;
        while ((ch=buffinp.read()) != -1) {
            buffoup.write(ch);
        }
        buffinp.close();
        flinp.close();
        buffoup.close();
        out.close();

    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}