package test.http.client;

import dive.http.client.ClientHttp;
import dive.http.common.MimeHttp;
import dive.http.common.MimeRequest;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ClientHttpTest {

    private MimeHttp http = new ClientHttp();

    @Test
    public void test() {
        new MimeRequest.Builder()
                .url("https://www.baidu.com/s?wd={1}_{name}")
                .url("http://www.baidu.com/s?wd={1}_{name}")
                .url("http://www.huobi.com")
                .url("https://www.huobi.com")
                .replace(123)
                .replace("name", 456)
                .get()
                .body("name", "123")
                .body("name2", "123")
                .build()
                .execute(http, (url, method, request, body, code, response, result) -> {
                    System.out.println("url: " + url);
                    System.out.println("method: " + method);
                    System.out.println("request: ");
                    if (null != request) request.forEach((k, v) -> System.out.println(k + " --> " + v));
                    System.out.println("body:\n" + body);
                    System.out.println("code: " + code);
                    System.out.println("response:");
                    if (null != response) response.forEach((k, v) -> System.out.println(k + " --> " + v));
                    System.out.println("result: \n" + result);
                });
    }

}
