package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private Map<String,String> headers = new HashMap<String, String>();
    private Map<String,String> params = new HashMap<String, String>();
    private RequestLine requestLine;

    public HttpRequest(InputStream in) {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            String line = br.readLine();

            requestLine = new RequestLine(line);

            line = br.readLine();
            while(!line.equals("")){
                log.debug("header line : {}",line);
                String[] tokens = line.split(":");
                if(tokens.length == 2){
                    headers.put(tokens[0], tokens[1]);
                }
                line = br.readLine();
            }

            if("POST".equals(getMethod())){
                String body = IOUtils.readData(br,Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }else{
                params = requestLine.getParams();
            }
        }catch (IOException io){
            log.error(io.getMessage());
        }
    }


    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }
    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}
