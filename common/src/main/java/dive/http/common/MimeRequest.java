package dive.http.common;


import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象，封装请求数据
 * @author dawn
 */
public class MimeRequest {

    /**
     * 请求的url
     */
    private String url;

    /**
     * 请求方法
     */
    private Method method = Method.GET;

    /**
     * 请求头
     */
    private Header header;

    /**
     * 请求体，字符串形式
     */
    private String string;

    /**
     * 请求体，键值对形式
     */
    private Parameter parameter;


    /**
     * url替换，例如：{1} --> name  {name} -> alice
     */
    private Map<String, Object> replace;

    /**
     * 额外和请求无关的内容
     */
    private String extra;

    /**
     * 构造器
     * @param url 请求url
     */
    private MimeRequest(String url) {
        this.url = url;
    }

    /**
     * 获取url, 替换数据
     * @return url
     */
    public String getUrl() {
        if (null != replace && 0 < replace.size()) {
            for (String name : replace.keySet()) {
                url = url.replace("{" + name + "}", replace.get(name).toString());
            }
            replace = null;
        }
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public Header getHeader() {
        return header;
    }


    public String getString() {
        return string;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public String getExtra() {
        return extra;
    }

    /**
     * 执行请求，获取结果
     * @param http 执行请求的对象
     * @return 请求结果
     */
    public String execute(MimeHttp http) {
        return execute(http, null);
    }

    /**
     * 执行请求，获取结果
     * @param http 执行请求的对象
     * @param detail 更详细的处理结果
     * @return 请求结果
     */
    public String execute(MimeHttp http, Detail detail) {
        return http.execute(this, detail);
    }

    /**
     * 获取建造者实例
     * @return 建造者实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 建造者类，用于创建MimeRequest对象
     */
    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private Header header;
        private String string;
        private Parameter parameter;

        private Map<String, Object> replace;
        private String extra;

        /**
         * 构造器
         */
        public Builder() { }

        /**
         * 构造器
         * @param url 请求url
         */
        public Builder(String url) {
            this.url = url;
        }

        /**
         * 设置url
         * @param url url
         * @return Builder
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * 设置url中的待替换变量
         * @param variable 变量值
         * @return Builder
         */
        public Builder replace(Object variable) {
            if (null == replace) {
                replace = new HashMap<>(4);
            }
            replace.put(String.valueOf(replace.size() + 1), variable);
            return this;
        }

        /**
         * 设置url中的待替换变量
         * @param name 变量名
         * @param variable 变量值
         * @return Builder
         */
        public Builder replace(String name, Object variable) {
            if (null == replace) {
                replace = new HashMap<>(4);
            }
            replace.put(name, variable);
            return this;
        }

        /**
         * 设置请求方法
         * @param method 请求方法
         * @return Builder
         */
        public Builder method(Method method) {
            if (null == method) {
                return this;
            }
            switch (method) {
                case GET:       this.method = Method.GET;       break;
                case POST:      this.method = Method.POST;      break;
                case PUT:       this.method = Method.PUT;       break;
                case DELETE:    this.method = Method.DELETE;    break;
                default:
            }
            return this;
        }

        /**
         * 设置为get请求
         * @return Builder
         */
        public Builder get() {
            return method(Method.GET);
        }

        /**
         * 设置为post请求
         * @return Builder
         */
        public Builder post() {
            //post请求必须有请求体
            if (null == string && null == parameter) {
                string = "";
            }
            return method(Method.POST);
        }

        /**
         * 设置为put请求
         * @return Builder
         */
        public Builder put() {
            return method(Method.PUT);
        }

        /**
         * 设置为delete请求
         * @return Builder
         */
        public Builder delete() {
            return method(Method.DELETE);
        }

        /**
         * 设置header
         * @param key 键
         * @param value 值
         * @return Builder
         */
        public Builder header(String key, String value) {
            if (null == header) {
                header = Header.build();
            }
            header.add(key, value);
            return this;
        }

        /**
         * 设置header
         * @param header header
         * @return Builder
         */
        public Builder header(Header header) {
            if (null == this.header) {
                this.header = header;
            } else {
                header.each(this.header::add);
            }
            return this;
        }

        /**
         * 设置字符串请求体
         * @param body 字符串请求体
         * @return Builder
         */
        public Builder body(String body) {
            this.string = body;
            return this;
        }

        /**
         * 设置键值对请求体
         * @param key 键
         * @param value 值
         * @return Builder
         */
        public Builder body(String key, Object value) {
            if (null == parameter) {
                parameter = Parameter.build(key, value);
            } else {
                parameter.add(key, value);
            }
            // 有键值对请求体就不需要字符串请求体了
            string = null;
            return this;
        }

        /**
         * 设置键值对请求体
         * @param parameter 键值对
         * @return Builder
         */
        public Builder body(Parameter parameter) {
            if (null == this.parameter) {
                this.parameter = parameter;
            } else {
                parameter.forEach(this.parameter::add);
            }
            // 有键值对请求体就不需要字符串请求体了
            string = null;
            return this;
        }

        /**
         * 额外需要的内容
         * @param extra extra
         * @return Builder
         */
        public Builder extra(String extra) {
            this.extra = extra;
            return this;
        }

        /**
         * 构建MimeRequest对象
         * @return MimeRequest对象
         */
        public MimeRequest build() {
            MimeRequest request = new MimeRequest(this.url);
            request.method = this.method;
            request.header = this.header;
            request.string = this.string;
            request.parameter = this.parameter;
            request.replace = this.replace;
            request.extra = this.extra;
            return request;
        }

        /**
         * 执行请求，获取结果
         * @param http 执行请求的对象
         * @return 请求结果
         */
        public String execute(MimeHttp http) {
            return http.execute(this.build());
        }

        /**
         * 执行请求，获取结果
         * @param http 执行请求的对象
         * @param detail 更详细的处理结果
         * @return 请求结果
         */
        public String execute(MimeHttp http, Detail detail) {
            return http.execute(this.build(), detail);
        }
    }
}
