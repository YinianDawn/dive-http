package dive.http.common;


import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 执行请求接口
 * @author dawn
 */
public interface MimeHttp {

    /**
     * 执行请求
     * @param request MimeRequest请求对象
     * @param detail 更加详细的处理
     * @return 请求结果
     */
    String execute(MimeRequest request, Detail detail);

    /**
     * 执行请求
     * @param request MimeRequest请求对象
     * @return 请求结果
     */
    default String execute(MimeRequest request) {
        return this.execute(request, null);
    }

    /**
     * 执行请求
     * @param request MimeRequest请求对象
     * @param change 将字符串转变为想要对象的函数
     * @return 转换后的实例对象
     */
    default <R> R execute(MimeRequest request, Function<String, R> change, Detail detail) {
        String result = this.execute(request, detail);
        if (null == result) {
            return null;
        }
        Objects.requireNonNull(change, "change");
        return change.apply(result);
    }

    /**
     * 执行请求
     * @param request MimeRequest请求对象
     * @param change 将字符串转变为想要对象的函数
     * @param clazz 结果对象的类型
     * @param detail 更加详细的处理
     * @return 转换后的实例对象
     */
    default <R> R execute(MimeRequest request, BiFunction<String, Class, R> change,
                          Class clazz, Detail detail) {
        String result = this.execute(request, detail);
        if (null == result) {
            return null;
        }
        Objects.requireNonNull(change, "change");
        return change.apply(result, clazz);
    }

    /**
     * 执行请求
     * @param request MimeRequest请求对象
     * @param change 将字符串转变为想要对象的函数
     * @param clazz 结果对象的类型
     * @return 转换后的实例对象
     */
    default <R> R execute(MimeRequest request,
                          BiFunction<String, Class, R> change, Class clazz) {
        return this.execute(request, change, clazz, null);
    }

    /**
     * 获取请求url，对url的参数进行encode
     * @param request MimeRequest对象
     * @return url
     */
    static String url(MimeRequest request) {
        String url = request.getUrl();
        if (request.getMethod() == Method.GET) {
            StringBuilder sb = new StringBuilder();
            if (null != request.getParameter()) {
                Parameter parameter = request.getParameter();
                for (String key : parameter.keySet()) {
                    Object o = parameter.get(key);
                    if (null == o) {
                        continue;
                    }
                    String name = Util.encode(key);
                    String value = Util.formatValue(o);
                    sb.append(name).append("=").append(value).append("&");
                }
                int length = sb.length();
                if (0 < length) {
                    sb.deleteCharAt(length - 1);
                }
            } else if (null != request.getString()) {
                sb.append(Util.encode(request.getString()));
            }

            if (0 < sb.length()) {
                String param = sb.toString();
                if (url.contains("?")) {
                    if (url.endsWith("&")) {
                        url += param;
                    } else {
                        url += "&" + param;
                    }
                } else {
                    url += "?" + param;
                }
            }
        }
        return url;
    }

    /**
     * 处理错误日志
     * @param message 日志
     */
    void error(String message);

    /**
     * 异常入职
     * @param e 异常
     * @param type 实现对象名称
     * @param url 请求url
     */
    default void error(Exception e, String type, String url) {
        error("MimeHttp " + type + " " + e.getClass().getSimpleName() + ": " + url);
    }

    /**
     * 异常处理
     * @param message 异常信息
     * @param detail 更加详细的处理
     * @param url 请求url
     * @param method 请求方法
     * @param request 请求header
     * @param body 请求体
     * @param code 结果码
     * @param response 响应header
     * @param result 请求结果
     * @return 请求结果
     */
    static String except(String message, Detail detail,
                         String url,
                         Method method,
                         Header request,
                         Object body,
                         Integer code,
                         Header response,
                         String result) {
        String cause = null;
        switch (message) {
            case "Connection timed out: connect": cause = " REQUEST TIMEOUT"; break;
            case "Connection refused: connect": cause = " CONNECTION REFUSED"; break;
            default:
        }
        switch (message) {
            case "Connection timed out: connect":
            case "Connection refused: connect":
                code = 408;
                result = code + cause;
                if (null != detail) {
                    detail.accept(url, method, request, body, code, response, result);
                }
                break;
                default:
        }
        return result;
    }
}
