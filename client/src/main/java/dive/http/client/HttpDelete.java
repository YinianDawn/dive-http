package dive.http.client;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * apache的包HttpDelete不能传请求体，自己写一个
 * @author dawn
 */
public class HttpDelete extends HttpEntityEnclosingRequestBase {

    private static final String METHOD_NAME = "DELETE";

    @Override
    public String getMethod() { return METHOD_NAME; }

    public HttpDelete(final String uri) {
        super();
        setURI(URI.create(uri));
    }
}