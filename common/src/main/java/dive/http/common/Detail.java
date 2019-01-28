package dive.http.common;


import dive.http.common.model.Header;
import dive.http.common.model.Method;

/**
 * 若请求需要获取更多信息，则传入该函数接口
 * @author dawn
 * @date 2019/01/27 13:18
 */
@FunctionalInterface
public interface Detail {

    /**
     * 请求回调
     * @param url 请求url
     * @param method 请求方法
     * @param request 请求头
     * @param body 请求体
     * @param code 响应码
     * @param response 响应体
     * @param result 请求结果
     */
    void accept(
            String url,
            Method method,
            Header request,
            Object body,
            Integer code,
            Header response,
            String result
    );

}
