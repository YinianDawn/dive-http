package dive.http.common.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 字符串键值对
 * @author dawn
 * @date 2019/01/27 13:18
 */
public class Pair implements Map.Entry<String, String>, Serializable {

    private static final long serialVersionUID = 539720049432666221L;

    private String key;
    private String value;

    public Pair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        if (null == value) {
            return null;
        }
        String old = this.value;
        this.value = value;
        return old;
    }
}
