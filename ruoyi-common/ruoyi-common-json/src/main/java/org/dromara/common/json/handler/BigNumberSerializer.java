package org.dromara.common.json.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * 超出 JS 最大最小值 处理
 *
 * @author Lion Li
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 得来
     * <p>
     * 注意：Java 和 MySQL 的 BigInt 类型支持更大的整数，范围为 {@code -2^63 + 1} 到 {@code 2^63 - 1}，
     * 超出了 JS 的最大安全整数范围 {@code 2^53 - 1L}。
     * 因此，Java 和 MySQL 可以处理比 JavaScript 安全整数范围更大的数值。
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    /**
     * 根据 JS Number.MIN_SAFE_INTEGER 得来
     * <p>
     * 注意：Java 和 MySQL 的 BigInt 类型支持更大的整数，范围为 {@code -2^63 + 1} 到 {@code 2^63 - 1}，
     * 超出了 JS 的最大安全整数范围 {@code -2^53 - 1L}。
     * 因此，Java 和 MySQL 可以处理比 JavaScript 安全整数范围更大的数值。
     */
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 提供实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 超出范围 序列化位字符串
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            super.serialize(value, gen, provider);
        } else {
            gen.writeString(value.toString());
        }
    }
}
