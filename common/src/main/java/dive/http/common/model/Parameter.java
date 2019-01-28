package dive.http.common.model;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * 请求参数
 * @author dawn
 * @date 2019/01/27 13:18
 */
public class Parameter extends LinkedHashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 6657828482402392084L;

    private Parameter() {}

    /**
     * 构造器
     * @return 实例对象
     */
    public static Parameter build() {
        return new Parameter();
    }

    /**
     * 构造器
     * @param key 键
     * @param value 值
     * @return 实例对象
     */
    public static Parameter build(String key, Object value) {
        Parameter parameter = new Parameter();
        parameter.put(key, value);
        return parameter;
    }

    /**
     * 添加键值对
     * @param key 键
     * @param value 值
     * @return 本实例对象
     */
    public Parameter add(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 排序
     * @param comparator 比较器
     * @return 本实例对象
     */
    public Parameter sort(Comparator<String> comparator) {
        Parameter parameter = new Parameter();
        this.keySet().stream()
                .sorted(comparator)
                .forEach(k -> parameter.add(k, this.get(k)));
        this.clear();
        parameter.forEach(this::add);
        return this;
    }

    /**
     * 排序，默认按字符串ASCII排序
     * @return 本实例对象
     */
    public Parameter sort() {
        return sort(String::compareTo);
    }

    /**
     * 连接
     * @param delimiter 键和值之间的符号
     * @param join 每对键值之间的符号
     * @param encode 编码函数
     * @return 请求参数连接结果
     */
    public String concat(String delimiter, String join, Function<Object, String> encode) {
        Objects.requireNonNull(delimiter, "delimiter");
        Objects.requireNonNull(join, "join");
        if (null == encode) {
            encode = Object::toString;
        }
        StringBuilder sb = new StringBuilder();
        Function<Object, String> finalEncode = encode;
        this.forEach((k, v) -> sb.append(k).append(delimiter)
                .append(finalEncode.apply(v)).append(join));
        String result = sb.toString();
        if (0 < sb.length()) {
            result = result.substring(0, result.length() - join.length());
        }
        return result;
    }

    /**
     * 连接
     * @param encode 编码函数
     * @return 连接结果
     */
    public String concat(Function<Object, String> encode) {
        return concat("=", "&", encode);
    }

    /**
     * 连接
     * @return 连接结果
     */
    public String concat() {
        return concat("=", "&", null);
    }

    /**
     * 转换json格式
     * @param function 转换函数
     * @return json字符串对象
     */
    public String json(Function<Map, String> function) {
        return function.apply(this);
    }

}
