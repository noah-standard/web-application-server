package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            log.debug("Request : {}", line);

            if (line == null) {
                return;
            }

            String[] tokens = line.split(" ");
            boolean isLogin = false;
            int contentLength = 0;
            while (!line.equals("")) {
                log.debug("header : {}", line);
                line = br.readLine();
                if (line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }
                if (line.contains("Cookie")) {
                    isLogin = isLogin(line);
                }
            }

            String url = tokens[1];
            byte[] body = new byte[0];
            DataOutputStream dos = new DataOutputStream(out);
            try {
                if ("/user/create".equals(url)) {
                    String paramsString = IOUtils.readData(br, contentLength);
                    Map<String, String> params = HttpRequestUtils.parseQueryString(paramsString);
                    User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                    log.debug("user : {}", user);
                    DataBase.addUser(user);
                    response302Header(dos, "/index.html");
                } else if ("/user/login".equals(url)) {
                    String paramsString = IOUtils.readData(br, contentLength);
                    Map<String, String> params = HttpRequestUtils.parseQueryString(paramsString);
                    User user = DataBase.findUserById(params.get("userId"));
                    if (user == null) {
                        reseponseResource(out, "user/login_failed.html");
                        return;
                    }

                    if (user.getPassword().equals(params.get("password"))) {
                        response302LoginSuccessHeader(dos);
                    } else {
                        reseponseResource(out, "/user/login_failed.html");
                    }
                } else if ("/user/list.html".equals(url)) {
                    if (!isLogin) {
                        reseponseResource(out, "/user/login.html");
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
                    byte[] bytes = sb.toString().getBytes();
                    response200Header(dos, bytes.length);
                    responseBody(dos, bytes);
                } else if (url.endsWith(".css")) {
                    body = Files.readAllBytes(new File("./webapp" + url).toPath());
                    reseponse200CssHeader(dos, body.length);
                    responseBody(dos, body);
                } else {
                    reseponseResource(out, url);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                body = "Hello World".getBytes();
                response200Header(dos, body.length);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void reseponse200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void reseponseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String cookie) {
        String[] headerTokens = cookie.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        return "true".equals(value);
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
