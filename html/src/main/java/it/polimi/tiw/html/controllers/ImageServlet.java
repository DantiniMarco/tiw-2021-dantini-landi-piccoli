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

import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;

@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        FileInputStream flinp;
        try {
            flinp = new FileInputStream(System.getProperty("catalina.home") + File.separator + "img" + File.separator
                    + request.getParameter("name"));
        }catch(IOException e){
            System.out.println(getServletContext().getRealPath("/img"));
            flinp = new FileInputStream(getServletContext().getRealPath("/img") + File.separator
                    + "noimage.png");
        }

        try (BufferedOutputStream buffoup = new BufferedOutputStream(out);BufferedInputStream buffinp = new BufferedInputStream(flinp)) {
            int ch = 0;
            while ((ch = buffinp.read()) != -1) {
                buffoup.write(ch);
            }
        }
        flinp.close();
        out.close();

    }
}