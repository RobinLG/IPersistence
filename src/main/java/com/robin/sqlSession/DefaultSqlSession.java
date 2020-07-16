package com.robin.sqlSession;

import com.robin.pojo.Configuration;
import com.robin.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementId, Object... params) throws SQLException, IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, InvocationTargetException, ClassNotFoundException {
        // Generating class 'simpleExecutor' to execute function 'query'.
        simpleExecutor simpleExecutor = new simpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);
        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(String statementId, Object... params) throws SQLException, IllegalAccessException, IntrospectionException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchFieldException {
        List<Object> objects = selectList(statementId, params);
        if (objects.size() == 1) {
            return (T) objects.get(0);
        } else {
            throw new RuntimeException("null or too many result");
        }
    }

    @Override
    public <T> T getMapper(Class<T> mapperClass) {
        // using AOP to generate object for DAO interface
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // executing JDBC code.
                // executing selectOne or selectList according according difference situation.
                // To prepare parameters 'statementId', statementId = 'Reference of interface' + '.' + 'name of method'.
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();
                String statementId = methodName + "." + className;

                // getting 'return value type' of function
                Type genericReturnType = method.getGenericReturnType();
                // whether generics?
                if (genericReturnType instanceof ParameterizedType) {
                    List<Object> objects = selectList(statementId, args);
                    return objects;
                }
                return selectOne(statementId, args);
            }
        });
        return null;
    }
}
