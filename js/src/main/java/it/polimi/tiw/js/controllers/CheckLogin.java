package it.polimi.tiw.js.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.UserDAO;
import org.apache.commons.text.StringEscapeUtils;
import it.polimi.tiw.js.utils.ConnectionHandler;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CheckLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // obtain and escape params
        String usrn = null;
        String pwd = null;
        usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
        pwd = StringEscapeUtils.escapeJava(request.getParameter("password"));
        if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty() ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Credentials must be not null");
            return;
        }
        // query db to authenticate for user
        UserDAO userDao = new UserDAO(connection);
        User user = null;
        try {
            user = userDao.checkCredentials(usrn, pwd);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal server error, retry later");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message

        // If the user exists, add info to the session and go to home page, otherwise
        // return an error status code and message
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Incorrect credentials");
        } else {
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(usrn);
        }

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