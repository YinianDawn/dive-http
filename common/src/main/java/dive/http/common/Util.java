package dive.http.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.*;

/**
 * 工具
 * @author dawn
 */
class Util {

    /**
     * url encode
     * @param s 待编码的字符串
     * @return 编码后的字符串
     */
    static String encode(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串流进行json数组格式化处理
     * @param stream 字符串流
     * @return json数组格式
     */
    private static String toArrayString(Stream<String> stream) {
        return "[" + stream.collect(Collectors.joining(",")) + "]";
    }

    /**
     * 将参数值进行格式化，主要针对数组列表集合等类型的json数组格式化处理
     * @param o 待格式化的值
     * @return 格式化后的请求参数的值
     */
    @SuppressWarnings("unchecked")
    static String formatValue(Object o) {
        try {
            if (o.getClass().isArray()) {
                switch (o.getClass().getTypeName()) {
                    case "int[]":
                        return toArrayString(IntStream.of((int[]) o).mapToObj(String::valueOf));
                    case "long[]":
                        return toArrayString(LongStream.of((long[]) o).mapToObj(String::valueOf));
                    case "double[]":
                        return toArrayString(DoubleStream.of((double[]) o).mapToObj(String::valueOf));
                    case "float[]":
                        List<Float> floats = new LinkedList<>();
                        for (float f : (float[]) o) {
                            floats.add(f);
                        }
                        return toArrayString(floats.stream().map(String::valueOf));
                    case "char[]":
                        List<Character> characters = new LinkedList<>();
                        for (char c : (char[]) o) {
                            characters.add(c);
                        }
                        return toArrayString(characters.stream().map(String::valueOf));
                    case "boolean[]":
                        List<Boolean> booleans = new LinkedList<>();
                        for (boolean b : (boolean[]) o) {
                            booleans.add(b);
                        }
                        return toArrayString(booleans.stream().map(String::valueOf));
                    case "short[]":
                        List<Short> shorts = new LinkedList<>();
                        for (short s : (short[]) o) {
                            shorts.add(s);
                        }
                        return toArrayString(shorts.stream().map(String::valueOf));
                    case "byte[]":
                        List<Byte> bytes = new LinkedList<>();
                        for (byte b : (byte[]) o) {
                            bytes.add(b);
                        }
                        return toArrayString(bytes.stream().map(String::valueOf));
                        default:
                }
                return toArrayString(Arrays.stream((Object[]) o).map(Util::formatValue));
            } else if (o instanceof Collection) {
                return toArrayString(((Collection) o).stream().map(Util::formatValue));
            } else {
                return URLEncoder.encode(o.toString(), "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
