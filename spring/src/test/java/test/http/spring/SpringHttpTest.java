package test.http.spring;

import dive.http.common.MimeHttp;
import dive.http.common.MimeRequest;
import dive.http.spring.SpringHttp;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringHttpTest {

    private MimeHttp http = new SpringHttp();

    @Test
    public void test() {
        new MimeRequest.Builder()
                .url("http://www.baidu.com/")
                .url("https://www.baidu.com/s?wd={1}_{name}")
                .url("http://www.huobi.com")
                .url("https://www.huobi.com")
                .replace(123)
                .replace("name", 456)
                .get()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36")
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
