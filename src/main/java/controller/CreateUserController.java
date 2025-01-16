package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;

public class CreateUserController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
        logger.debug("user : {}", user);
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }
}
