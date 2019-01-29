package dive.http.common.model;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * http请求头
 * @author dawn
 */
public class Header extends HashMap<String, List<String>> implements Serializable {

    private static final long serialVersionUID = -5369345064482266035L;

    private Header() {}

    /**
     * 获取实例
     * @return 实例
     */
    public static Header build() {
        return new Header();
    }

    /**
     * 获取实例并存入第一组数据
     * @param key 键
     * @param value 值
     * @return 对象
     */
    public static Header build(String key, String value) {
        Header header = new Header();
        List<String> values = new LinkedList<>();
        values.add(value);
        header.put(key, values);
        return header;
    }

    /**
     * 数据组数
     * @return 组数
     */
    @Override
    public int size() {
        return super.values().stream()
                .mapToInt(Collection::size)
                .sum();
    }

    /**
     * 是否包含键
     * @param key 键
     * @return 是否包含键
     */
    public boolean containsKey(String key) {
        return null != key && super.containsKey(key);
    }

    /**
     * 是否包含值
     * @param value 值
     * @return 是否包含值
     */
    public boolean containsValue(String value) {
        if (null == value) {
            return false;
        }
        for (List<String> values : super.values()) {
            for (String v : values) {
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    public List<String> get(String key) {
        if (null == key) {
            return null;
        }
        List<String> values = super.get(key);
        if (null == values || 0 == values.size()) {
            return null;
        }
        return values;
    }

    /**
     * 添加一组键值对，若存在键，则替换
     * @param key 键
     * @param value 值
     * @return 本实例
     */
    public Header put(String key, String value) {
        if (null != key && null != value) {
            List<String> values = new LinkedList<>();
            values.add(value);
            super.put(key, values);
        }
        return this;
    }

    /**
     * 添加一组键值对，若存在键，不替换
     * @param key 键
     * @param value 值
     * @return 本实例
     */
    public Header add(String key, String value) {
        if (null != key && null != value) {
            List<String> values = super.computeIfAbsent(key, k -> new LinkedList<>());
            values.add(value);
        }
        return this;
    }

    /**
     * 移除键
     * @param key 键
     */
    public void remove(String key) {
        if (null == key) {
            return;
        }
        super.remove(key);
    }

    /**
     * 获取所有键值对
     * @return 键值对列表
     */
    public List<Pair> pairs() {
        return super.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream()
                        .map(v -> new Pair(e.getKey(), v)))
                .collect(Collectors.toList());
    }

    /**
     * 遍历所有键值对
     * @param action 对键值进行的操作
     */
    public void each(BiConsumer<String, String> action) {
        Objects.requireNonNull(action, "action");
        for (Pair pair : this.pairs()) {
            action.accept(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public String toString() {
        return "Header{" +
                this.pairs().stream()
                        .map(p -> "\"" + p.getKey() + "\":\""
                                + p.getValue() + "\"")
                        .collect(Collectors.joining(","))
                + "}";
    }
}
