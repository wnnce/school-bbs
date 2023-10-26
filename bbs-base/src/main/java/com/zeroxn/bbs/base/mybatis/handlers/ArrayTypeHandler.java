package com.zeroxn.bbs.base.mybatis.handlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;

import java.sql.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-16 19:28:02
 * @Description: 自定义mybatis的类型处理器 处理数组和数据库Array类型的转换
 */
public class ArrayTypeHandler extends BaseTypeHandler<Object[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object[] parameter, JdbcType jdbcType) throws SQLException {
        String typename = null;
        if (parameter instanceof Integer[]) {
            typename = "integer";
        } else if (parameter instanceof String[]) {
            typename = "varchar";
        } else if (parameter instanceof Long[]) {
            typename = "bigint";
        }
        if (typename == null) {
            throw new TypeException("ArrayTypeHandle error, type:" + parameter.getClass().getName());
        }
        Array array = ps.getConnection().createArrayOf(typename, parameter);
        ps.setArray(i, array);
    }

    @Override
    public Object[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArray(rs.getArray(columnName));
    }


    @Override
    public Object[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArray(rs.getArray(columnIndex));
    }


    @Override
    public Object[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArray(cs.getArray(columnIndex));
    }

    private Object[] getArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        return (Object[]) array.getArray();
    }
}
