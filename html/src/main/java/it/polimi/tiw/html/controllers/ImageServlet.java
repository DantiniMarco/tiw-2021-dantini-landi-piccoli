package it.polimi.tiw.html.controllers;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ImageServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        response.setContentType("image/jpeg");
        System.out.println(request.getParameter("name"));
        System.out.println(System.getProperty("upload.location") + File.separator + "img" + File.separator
                + request.getParameter("name"));
        InputStream flinp;
        try {
            flinp = new FileInputStream(System.getProperty("upload.location") + File.separator + "img" + File.separator
                    + request.getParameter("name"));
        }catch(IOException e){
            flinp = getServletContext().getResourceAsStream("/img/noimage.png");
        }

        try (ServletOutputStream out = response.getOutputStream(); BufferedOutputStream buffoup = new BufferedOutputStream(out);BufferedInputStream buffinp = new BufferedInputStream(flinp)) {
            int ch;
            while ((ch = buffinp.read()) != -1) {
                buffoup.write(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            flinp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}