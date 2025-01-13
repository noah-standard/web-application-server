package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos = null;
    private Map<String,String> headers = new HashMap<String, String>();

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void addHeader(String name,String value){
        headers.put(name, value);
    }

    public void forward(String path) throws IOException {
       try{
           byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
           if(path.endsWith(".css")){
               headers.put("Content-Type","text/css");
           }else if(path.endsWith(".js")){
               headers.put("Content-Type","text/javascript");
           } else {
               headers.put("Content-Type","text/html");
           }
           headers.put("Content-Length",String.valueOf(body.length));
           response200Header(body.length);
           responseBody(body);
       }catch (IOException e){
           log.error(e.getMessage());
       }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        headers.put("Content-Length",String.valueOf(contents.length));
        headers.put("Content-Type","text/html;charset=UTF-8");
        response200Header(contents.length);
        responseBody(contents);
    }

    public void sendRedirect(String redirectUrl){
        try{
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            processHeaders();
            dos.writeBytes("Location: " + redirectUrl + " \r\n");
            dos.writeBytes("\r\n");
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void response200Header(int lengthOfBodyContent){
        try{
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {log.error(e.getMessage());}
    }

    private void processHeaders(){
        try{
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                dos.writeBytes(key + ": " + headers.get(key) + " \r\n");
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

}
