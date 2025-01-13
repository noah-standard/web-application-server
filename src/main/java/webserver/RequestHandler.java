package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String path = getDefaultPath(request.getPath());

            try {
                if ("/user/create".equals(path)) {
                    User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
                    log.debug("user : {}", user);
                    DataBase.addUser(user);
                    response.sendRedirect("/index.html");
                } else if ("/user/login".equals(path)) {
                    User user = DataBase.findUserById(request.getParameter("userId"));
                    if (user != null) {
                        if (user.getPassword().equals(request.getParameter("password"))) {
                            response.addHeader("Set-Cookie", "logined=true");
                            response.sendRedirect("/index.html");
                        } else {
                            response.sendRedirect("/user/login_failed.html");
                        }
                    }else{
                        response.sendRedirect("/user/login_failed.html");
                    }
                } else if ("/user/list.html".equals(path)) {
                    if (!isLogin(request.getHeader("Cookie"))) {
                        response.sendRedirect( "/user/login.html");
                        return;
                    }
                    Collection<User> all = DataBase.findAll();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><head><title>User List</title></head><body>");
                    sb.append("<table border='1'>");
                    sb.append("<tr><th>userId</th><th>name</th><th>email</th></tr>");
                    for (User user : all) {
                        sb.append("<tr><td>").append(user.getUserId()).append("</td><td>").append(user.getName())
                                .append("</td><td>").append(user.getEmail()).append("</td></tr>");
                    }
                    sb.append("</table>");
                    sb.append("</body></html>");
                    response.forwardBody(sb.toString());
                } else {
                    response.forward(path);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getDefaultPath(String path) {
        if(path.equals("/")){
            return "/index.html";
        }
        return path;
    }


    private boolean isLogin(String cookie) {
        String[] headerTokens = cookie.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        return "true".equals(value);
    }
}
