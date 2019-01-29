package dive.http.spring;

import dive.http.common.BaseMimeHttp;
import dive.http.common.Detail;
import dive.http.common.MimeHttp;
import dive.http.common.MimeRequest;
import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 利用Spring的RestTemplate实现
 */
public class SpringHttp extends BaseMimeHttp {

    /**
     * RestTemplate 实例
     */
    private final RestTemplate rest;

    /**
     * 构造器
     */
    public SpringHttp() {
        this.rest = new RestTemplate();
        rest.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    /**
     * 构造器
     * @param restTemplate RestTemplate实例
     */
    public SpringHttp(RestTemplate restTemplate) {
        this.rest = restTemplate;
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

        HttpMethod httpMethod = HttpMethod.valueOf(method.name());
        Object body = null;
        Integer code = null;
        Header responseHeader = null;
        String result = null;

        try {
            HttpHeaders headers = this.headers(request.getHeader());
            if (!HttpMethod.GET.equals(httpMethod)) {
                body = this.body(request);
            }
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = rest.exchange(url, httpMethod, entity, String.class);
            result = response.getBody();

            if (null != detail) {
                code = response.getStatusCodeValue();
                headers = response.getHeaders();
                Header finalResponseHeader = Header.build();
                headers.forEach((k, list) -> list.forEach(v -> finalResponseHeader.put(k, v)));
                detail.accept(url,
                        method,
                        requestHeader,
                        para,
                        code,
                        finalResponseHeader,
                        result);
            }
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            error(e, "RestTemplate", url);
            code = e.getRawStatusCode();
            if (403 == code) {
                error("maybe you should add 'User-Agent' for headers, for example: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
            }
            result = code + " " + e.getStatusText();
            HttpHeaders headers = e.getResponseHeaders();
            Header finalResponseHeader = Header.build();
            if (null != headers) {
                headers.forEach((k, list) -> list.forEach(v -> finalResponseHeader.put(k, v)));
            }
            if (null != detail) {
                detail.accept(url,
                        method,
                        requestHeader,
                        para,
                        code,
                        finalResponseHeader,
                        result);
            }
        } catch (ResourceAccessException e) {
            e.printStackTrace();
            error(e, "RestTemplate", url);
            result = MimeHttp.except(e.getCause().getMessage(), detail,
                    url,
                    method,
                    requestHeader,
                    para,
                    code,
                    responseHeader,
                    result);
        } catch (RestClientException e) {
            e.printStackTrace();
            error(e, "RestTemplate", url);
        }
        if (null == result) {
            error("RestTemplate result is null. url --> " + url);
        }
        return result;
    }

    private HttpHeaders headers(Header header) {
        if (null == header) {
            return null;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        header.each(httpHeaders::add);
        return httpHeaders;
    }

    private Object body(MimeRequest request) {
        if (null != request.getParameter()) {
            Parameter parameter = request.getParameter();
            MultiValueMap<String, Object> b = new LinkedMultiValueMap<>();
            parameter.forEach(b::add);
            return b;
        }
        if (null != request.getString()) {
            return request.getString();
        }
        super.error("can not recognize body:" + request);
        return null;
    }

}
