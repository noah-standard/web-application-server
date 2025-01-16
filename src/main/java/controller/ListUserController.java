package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
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
    }

    private boolean isLogin(String cookie) {
        String[] headerTokens = cookie.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        return "true".equals(value);
    }
}
