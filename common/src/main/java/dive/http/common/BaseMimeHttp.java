package dive.http.common;

import java.util.function.Consumer;

/**
 * 统一错误输出
 *
 * @author dawn
 */
public abstract class BaseMimeHttp implements MimeHttp {

    /**
     * 默认处理错误方式
     */
    protected Consumer<String> error = System.err::println;

    /**
     * 更改错误处理方法
     * @param error 错误消费者
     * @return 本实例
     */
    public BaseMimeHttp error(Consumer<String> error) {
        if (null != error) {
            this.error = error;
        }
        return this;
    }

    @Override
    public void error(String message) {
        this.error.accept(message);
    }

}
