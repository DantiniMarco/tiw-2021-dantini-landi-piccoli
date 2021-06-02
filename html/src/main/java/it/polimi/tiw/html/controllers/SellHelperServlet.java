package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@WebServlet("/SellHelperServlet")
@MultipartConfig
public class SellHelperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
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
        String deadlineLocalDateTime_param = request.getParameter("deadlineLocalDateTime");
        String deadlineTimeZone_param = request.getParameter("deadlineTimeZone");
        System.out.println(ZoneId.getAvailableZoneIds());
        boolean bad_request = false;
        float initialPrice = 0;
        float minRaise = 0;
        Timestamp deadline = null;
        java.util.Date date = null;
        AuctionDAO am = new AuctionDAO(connection);
        InputStream filecontent = null;
        System.out.println(System.getProperty("catalina.home"));
        if (itemName == null || itemName.isEmpty() || itemDescription == null || itemDescription.isEmpty()
                || initialPrice_param == null || initialPrice_param.isEmpty() || minRaise_param == null || minRaise_param.isEmpty()
                || deadlineLocalDateTime_param == null || deadlineLocalDateTime_param.isEmpty() || deadlineTimeZone_param == null || deadlineTimeZone_param.isEmpty()) {
            bad_request = true;
        }

        try {
            initialPrice = Float.parseFloat(initialPrice_param);
            minRaise = Float.parseFloat(minRaise_param);
            LocalDateTime dateWithTimeZone = LocalDateTime.parse(deadlineLocalDateTime_param, ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of(deadlineTimeZone_param)).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
            deadline = Timestamp.valueOf(dateWithTimeZone);
        } catch (NumberFormatException e) {
            bad_request = true;
        } catch (Exception e) {
            e.printStackTrace();
            bad_request = true;
        }
        UUID uuid = UUID.randomUUID();
        String newFileName = uuid.toString() + (fileName != "" ? fileName.substring(fileName.indexOf(".")) : "");
        if (filePart.getSize() > 0) {
            try (OutputStream out = new FileOutputStream(new File(System.getProperty("catalina.home") + File.separator + "img" + File.separator
                    + newFileName))) {

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
                bad_request = true;
            } finally {
                if (filecontent != null) {
                    filecontent.close();
                }
            /*if (writer != null) {
                writer.close();
            }*/
            }
        }

        if (minRaise < 0.1 || minRaise > 500000) {
            bad_request = true;
        }

        if (initialPrice < 1 || initialPrice > 999999.99) {
            bad_request = true;
        }

        if (bad_request == true) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter/s missing");
            throw new UnavailableException("Parameter/s missing");
        }

        User user = (User) request.getSession().getAttribute("user");
        int idCreator = user.getIdUser();
        if (fileName != null && fileName.isEmpty()) {
            newFileName = null;
        }
        try {
            am.insertNewAuction(itemName, newFileName, itemDescription, initialPrice, minRaise, deadline, idCreator);
        } catch (SQLException sqle) {
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
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }
}
