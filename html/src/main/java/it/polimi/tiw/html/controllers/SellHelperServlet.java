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
import java.time.*;
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
        float initialPrice;
        float minRaise;
        Timestamp deadline;
        AuctionDAO am = new AuctionDAO(connection);
        InputStream filecontent = null;
        String errorString = "";

        try {
            if (itemName == null || itemName.isEmpty() || itemDescription == null || itemDescription.isEmpty()
                    || initialPriceParam == null || initialPriceParam.isEmpty() || minRaiseParam == null || minRaiseParam.isEmpty()
                    || deadlineLocalDateTimeParam == null || deadlineLocalDateTimeParam.isEmpty() || deadlineTimeZoneParam == null || deadlineTimeZoneParam.isEmpty()) {
                System.out.println("Sono nel primo if");
                throw new NumberFormatException();
            }

            initialPrice = Float.parseFloat(initialPriceParam);
            minRaise = Float.parseFloat(minRaiseParam);
            LocalDateTime dateWithTimeZone = LocalDateTime.parse(deadlineLocalDateTimeParam, ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of(deadlineTimeZoneParam)).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
            deadline = Timestamp.valueOf(dateWithTimeZone);

            if (minRaise < 0.01f || minRaise > 500000) {
                throw new NumberFormatException();
            }
            if (initialPrice < 1 || initialPrice > 999999.99f) {
                throw new NumberFormatException();
            }

            LocalDateTime dateLowerBound = LocalDateTime.now(ZoneOffset.UTC);
            dateLowerBound = dateLowerBound.plusDays(1);
            LocalDateTime dateUpperBound = LocalDateTime.now(ZoneOffset.UTC);
            dateUpperBound = dateUpperBound.plusWeeks(2);
            if(deadline.before(Timestamp.valueOf(dateLowerBound)) || deadline.after(Timestamp.valueOf(dateUpperBound))){
                throw new DateTimeException("Wrong deadline");
            }

            UUID uuid = UUID.randomUUID();
            String newFileName = uuid + (fileName != "" ? fileName.substring(fileName.indexOf(".")) : "");
            if (filePart.getSize() > 0) {
                try (OutputStream out = new FileOutputStream(System.getProperty("upload.location") + File.separator + "img" + File.separator
                        + newFileName)) {

                    filecontent = filePart.getInputStream();

                    int read = 0;
                    final byte[] bytes = new byte[1024];

                    while ((read = filecontent.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }

                } catch (FileNotFoundException fne) {
                    System.out.println("Problems during file upload.");
                    System.out.println("Sono nel terzo catch");
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
                    return;
                } finally {
                    if (filecontent != null) {
                        filecontent.close();
                    }
                }
            }

            User user = (User) request.getSession().getAttribute("user");
            int idCreator = user.getIdUser();
            if (fileName.isEmpty()) {
                newFileName = null;
            }
            am.insertNewAuction(itemName, newFileName, itemDescription, initialPrice, minRaise, deadline, idCreator);


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
