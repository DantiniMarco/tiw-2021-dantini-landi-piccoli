package it.polimi.tiw.js.controllers;

import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/SellHelperServlet")
@MultipartConfig
public class SellHelperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        con = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String itemName = request.getParameter("itemName");
        final Part filePart = request.getPart("itemImage");
        String fileName = getFileName(filePart);
        System.out.println(fileName);
        String itemDescription = request.getParameter("itemDescription");
        String initialPrice_param = request.getParameter("initialPrice");
        String minRaise_param = request.getParameter("minRaise");
        String deadline_param = request.getParameter("deadline");
        int bad_request = 0;
        float initialPrice = 0;
        float minRaise = 0;
        Date deadline = null;
        AuctionDAO am = new AuctionDAO(con);
        OutputStream out = null;
        InputStream filecontent = null;
        System.out.println(System.getProperty("catalina.home"));
        if (itemName == null || itemName.isEmpty() || itemDescription == null || itemDescription.isEmpty()
                || initialPrice_param == null || initialPrice_param.isEmpty() || minRaise_param == null || minRaise_param.isEmpty() || deadline_param == null
                || deadline_param.isEmpty()) {
            bad_request = 1;
        }

        try {
            initialPrice = Float.parseFloat(initialPrice_param);
            minRaise = Float.parseFloat(minRaise_param);
            deadline = Date.valueOf(deadline_param);
        } catch (NumberFormatException e) {
            bad_request = 1;
        } catch (Exception e) {
            bad_request = 1;
        }

        if (filePart.getSize() > 0) {
            try {
                out = new FileOutputStream(new File(System.getProperty("catalina.home") + File.separator + "img" + File.separator
                        + fileName));
                filecontent = filePart.getInputStream();

                int read = 0;
                final byte[] bytes = new byte[1024];

                while ((read = filecontent.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                System.out.println("File{0}being uploaded to {1}");
            } catch (FileNotFoundException fne) {
            /*writer.println("You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.");*/
                //writer.println("<br/> ERROR: " + fne.getMessage());

                System.out.println("Problems during file upload. Error: {0}");
                bad_request = 1;
            } finally {
                if (out != null) {
                    out.close();
                }
                if (filecontent != null) {
                    filecontent.close();
                }
            /*if (writer != null) {
                writer.close();
            }*/
            }
        }
        if (bad_request == 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter/s missing");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        int idCreator = user.getIdUser();
        if(fileName != null && fileName.isEmpty()){
            fileName=null;
        }
        try{
            am.insertNewAuction(itemName,fileName, itemDescription, initialPrice, minRaise, deadline, idCreator);
        }catch (SQLException sqle){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
        }

        response.sendRedirect("SellServlet");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private String getFileName(final Part part) {
        System.out.println("Part Header = {0}");
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
