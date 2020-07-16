package com.robin.sqlSession;

import com.robin.config.BoundSql;
import com.robin.pojo.Configuration;
import com.robin.pojo.MappedStatement;
import com.robin.utils.GenericTokenParser;
import com.robin.utils.ParameterMapping;
import com.robin.utils.ParameterMappingTokenHandler;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class simpleExecutor implements Executor {

    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        // Registering driver to get the connection.
        Connection connection = configuration.getDataSource().getConnection();

        // Getting SQL. Analysing and storing the value in #{} for process of transformation.
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);

        // Getting preparedStatement.
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());

        // Setting parameter.
        // Getting path of variable.
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterTypeClass = getClassType(parameterType);

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String content = parameterMapping.getContent();

            // reflect
            Field declaredField = parameterTypeClass.getDeclaredField(content);
            // mandatory
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i + 1, o);
        }

        // Executing SQL.
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> classType = getClassType(resultType);
        ArrayList<Object> objects = new ArrayList<>();

        // Returning collection of result.
        while (resultSet.next()) {
            Object o = classType.newInstance();
            // meta data
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                // column name
                String columnName = metaData.getColumnName(i);
                // column value
                Object value = resultSet.getObject(columnName);

                // using reflect or IntroSpector
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, classType);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o, value);
            }
            objects.add(o);
        }
        return (List<E>) objects;
    }

    private Class<?> getClassType(String parameterType) throws ClassNotFoundException {
        if (parameterType != null) {
            Class<?> aClass = Class.forName(parameterType);
            return aClass;
        }
        return null;
    }

    /**
     * Completing the parser of #{}.
     * 1. ? instead of #{}
     * 2. Storing the value from #{}
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        // parsed SQL
        String parseSql = genericTokenParser.parse(sql);
        // Parsed parameter from #{}
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();

        BoundSql boundSql = new BoundSql(parseSql, parameterMappings);
        return boundSql;
    }
}
