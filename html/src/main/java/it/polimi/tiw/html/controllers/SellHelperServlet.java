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
        String initialPriceParam = request.getParameter("initialPrice");
        String minRaiseParam = request.getParameter("minRaise");
        String deadlineLocalDateTimeParam = request.getParameter("deadlineLocalDateTime");
        String deadlineTimeZoneParam = request.getParameter("deadlineTimeZone");
        System.out.println(ZoneId.getAvailableZoneIds());
        boolean bad_request = false;
        float initialPrice;
        float minRaise;
        Timestamp deadline;
        AuctionDAO am = new AuctionDAO(connection);
        InputStream filecontent = null;
        String errorString = "";

        System.out.println(System.getProperty("catalina.home"));
        try {
            if (itemName == null || itemName.isEmpty() || itemDescription == null || itemDescription.isEmpty()
                    || initialPriceParam == null || initialPriceParam.isEmpty() || minRaiseParam == null || minRaiseParam.isEmpty()
                    || deadlineLocalDateTimeParam == null || deadlineLocalDateTimeParam.isEmpty() || deadlineTimeZoneParam == null || deadlineTimeZoneParam.isEmpty()) {
                throw new NumberFormatException();
            }

            initialPrice = Float.parseFloat(initialPriceParam);
            minRaise = Float.parseFloat(minRaiseParam);
            LocalDateTime dateWithTimeZone = LocalDateTime.parse(deadlineLocalDateTimeParam, ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of(deadlineTimeZoneParam)).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
            deadline = Timestamp.valueOf(dateWithTimeZone);

            if (minRaise < 0.1 || minRaise > 500000 || initialPrice < 1 || initialPrice > 999999.99) {
                throw new NumberFormatException();
            }

            UUID uuid = UUID.randomUUID();
            String newFileName = uuid + (fileName != "" ? fileName.substring(fileName.indexOf(".")) : "");
            if (filePart.getSize() > 0) {
                try (OutputStream out = new FileOutputStream(System.getProperty("catalina.home") + File.separator + "img" + File.separator
                        + newFileName)) {

                    filecontent = filePart.getInputStream();

                    int read = 0;
                    final byte[] bytes = new byte[1024];

                    while ((read = filecontent.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }

                } catch (FileNotFoundException fne) {
                    System.out.println("Problems during file upload.");
                    bad_request = true;
                } finally {
                    if (filecontent != null) {
                        filecontent.close();
                    }
                }
            }

            if (bad_request) {
                errorString = "?error=wrongFormat";
            } else {
                User user = (User) request.getSession().getAttribute("user");
                int idCreator = user.getIdUser();
                if (fileName.isEmpty()) {
                    newFileName = null;
                }
                am.insertNewAuction(itemName, newFileName, itemDescription, initialPrice, minRaise, deadline, idCreator);

            }
        } catch (NumberFormatException e) {
            errorString = "?error=wrongFormat";
        } catch (SQLException sqle) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error");
            return;
        }
        response.sendRedirect(getServletContext().getContextPath() + "/SellServlet" + errorString);

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
