package com.zeroxn.bbs.base.mybatis.handlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 11:41:45
 * @Description: Postgresql数组字段转List转换器
 */
public class ArrayToListTypeHandler extends BaseTypeHandler<List<?>>{
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter, JdbcType jdbcType) throws SQLException {
        Object firstListItem = parameter.get(0);
        String typeName = null;
        if (firstListItem instanceof String) {
            typeName = "varchar";
        } else if (firstListItem instanceof Integer) {
            typeName = "integer";
        } else if (firstListItem instanceof Long) {
            typeName = "bigint";
        }
        if (typeName == null){
            throw new TypeException("ArrayTypeHandle error, type:" + parameter.getClass().getName());
        }
        Array array = ps.getConnection().createArrayOf(typeName, parameter.toArray());
        ps.setArray(i, array);
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArray(rs.getArray(columnName));
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArray(rs.getArray(columnIndex));
    }

    @Override
    public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArray(cs.getArray(columnIndex));
    }

    private List<?> getArray(Array array) throws SQLException{
        if (array == null) {
            return null;
        }
        return Arrays.stream(((Object[]) array.getArray())).toList();
    }
}
