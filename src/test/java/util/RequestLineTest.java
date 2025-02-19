package util;


import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RequestLineTest  {

    @Test
    public void create_method() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html",line.getPath());

        line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("/index.html",line.getPath());
    }

    @Test
    public void create_path_and_params() {
        RequestLine line = new RequestLine("GET /index.html?userId=javajigi HTTP/1.1");
        assertEquals("GET",line.getMethod());
        assertEquals("/index.html",line.getPath());
        Map<String, String> params = line.getParams();
        assertEquals(1, params.size());
    }
}