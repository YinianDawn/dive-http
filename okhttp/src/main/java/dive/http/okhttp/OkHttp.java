package dive.http.okhttp;

import dive.http.common.BaseMimeHttp;
import dive.http.common.Detail;
import dive.http.common.MimeHttp;
import dive.http.common.MimeRequest;
import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;
import okhttp3.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Okhttp实现
 * @author dawn
 */
public class OkHttp extends BaseMimeHttp {
    private static final MediaType PLAIN = MediaType.parse("text/plain; charset=utf-8");
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    /**
     * Okhttp实例
     */
    private OkHttpClient client;

    /**
     * 构造器
     * @param proxy 代理对象
     * @param authenticator 认证
     */
    public OkHttp(Proxy proxy, Authenticator authenticator) {
        client = new OkHttpClient.Builder().proxy(proxy).authenticator(authenticator).build();
    }

    /**
     * 构造器
     * @param proxy 代理对象
     */
    public OkHttp(Proxy proxy) {
        client = new OkHttpClient.Builder().proxy(proxy).build();
    }

    /**
     * 构造器
     * @param connect 设置连接超时时间
     * @param read 设置读取超时时间
     */
    public OkHttp(long connect, int read) {
        client = new OkHttpClient.Builder()       
            .connectTimeout(connect, TimeUnit.SECONDS)
            .readTimeout(read, TimeUnit.SECONDS)
            .build();
    }

    /**
     * 构造器
     */
    public OkHttp() {
        client = new OkHttpClient();
    }

    @Override
    public String execute(MimeRequest request, Detail detail) {
        Objects.requireNonNull(request, "mime request is null");

        String url = MimeHttp.url(request);
        Method method = request.getMethod();
        Header requestHeader = request.getHeader();
        Parameter parameter = request.getParameter();
        String string = request.getString();
        Object para = null != parameter ? parameter : string;

        RequestBody body = null;
        Response response = null;
        Integer code = null;
        Header responseHeader = Header.build();
        String result = null;

        try {
            if (!Method.GET.equals(method)) {
                body = body(request);
            }
            Request.Builder builder = new Request.Builder();
            Headers headers = headers(request.getHeader());
            if (null != headers) {
                builder.headers(headers);
            }
            builder.url(url);
            builder.method(method.name(), body);
            Request r = builder.build();
            response = client.newCall(r).execute();
            ResponseBody requestBody = response.body();
            if (null != requestBody) {
                result = requestBody.string();
            }
            if (null != detail) {
                code = response.code();
                headers = response.headers();
                headers.toMultimap().forEach((k, list) ->
                        list.forEach(v -> responseHeader.put(k, v)));
                detail.accept(url,
                        method,
                        requestHeader,
                        para,
                        code,
                        responseHeader,
                        result);
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            error(e, "Okhttp", url);
            result = MimeHttp.except("Connection timed out: connect", detail,
                    url,
                    method,
                    requestHeader,
                    para,
                    code,
                    responseHeader,
                    result);
        } catch (ConnectException e) {
            e.printStackTrace();
            error(e, "Okhttp", url);
            result = MimeHttp.except(e.getCause().getMessage(), detail,
                    url,
                    method,
                    requestHeader,
                    para,
                    code,
                    responseHeader,
                    result);
        } catch (IOException e) {
            e.printStackTrace();
            error(e, "Okhttp", url);
        } finally {
            if (null != response) {
                response.close();
            }
        }
        if (null == result) {
            error("Okhttp result is null. url --> " + url);
        }
        return result;
    }

    /**
     * 构造Okhttp用的header
     * @param header header
     * @return Headers
     */
    private Headers headers(Header header) {
        if (null == header) {
            return null;
        }
        Headers.Builder builder = new Headers.Builder();
        header.each(builder::add);
        return builder.build();
    }

    /**
     * 构造Okhttp用的body
     * @param request 请求对象
     * @return RequestBody
     */
    private RequestBody body(MimeRequest request) {
        if (null != request.getParameter()) {
            Parameter parameter = request.getParameter();
            FormBody.Builder builder = new FormBody.Builder();
            parameter.forEach((k, v) -> builder.add(k, v.toString()));
            return builder.build();
        } else if (null != request.getString()) {
            okhttp3.MediaType mt = JSON;
            if (!request.getString().trim().startsWith("{") && !request.getString().trim().startsWith("[")) {
                mt = PLAIN;
            }
            return RequestBody.create(mt, request.getString());
        }

        error("can not recognize body:" + request);
        return null;
    }

}
