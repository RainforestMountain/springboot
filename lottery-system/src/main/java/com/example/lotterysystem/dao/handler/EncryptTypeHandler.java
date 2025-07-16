package com.example.lotterysystem.dao.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//回归最初的问题，我们对⼿机号进⾏存储时，要先将⼿机号加密，如果要拿出使⽤时，还要进⾏⼀次
// * 解密操作。为了不让每次⼿动去加密解密，决定使⽤ Mybatis 的 TypeHandler 来解决。
// * TypeHandler : 简单理解就是当处理某些特定字段时，我们可以实现⼀些⽅法，让 Mybatis 遇到这些
// * 特定字段可以⾃动运⾏处理。
//@MappedTypes：表⽰该处理器处理的 java 类型是什么。
//@MappedJdbcTypes：表⽰处理器处理的 Jdbc 类型。
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(Encrypt.class)
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {
    //设置一个密钥
    private static final byte[] KEYS = "123456789hgf0000".getBytes(StandardCharsets.UTF_8);

    /**
     * 设置参数:加密
     *
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.getValue() == null) {
            ps.setString(i, null);
        }
        AES aes = SecureUtil.aes(KEYS);
        String encrypt = aes.encryptHex(parameter.getValue());
        ps.setString(i, encrypt);
    }

    /**
     * 获取值, 解密
     *
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    /**
     * 解密
     *
     * @param str
     * @return
     */
    private Encrypt decrypt(String str) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        return new Encrypt(SecureUtil.aes(KEYS).decryptStr(str));
    }
}
