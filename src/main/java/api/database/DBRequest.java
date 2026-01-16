package api.database;

import api.configs.Config;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DBRequest {
    private RequestType requestType;
    private String table;
    private List<Condition> conditions;
    private Class<?> extractAsClass;

    public enum RequestType {
        SELECT_AND, SELECT_OR, INSERT, UPDATE, DELETE
    }

    public <T> T extractAs(Class<T> clazz) {
        this.extractAsClass = clazz;
        return executeSingleQuery(clazz);
    }

    public <T> List<T> extractAsList(Class<T> clazz) {
        this.extractAsClass = clazz;
        return executeListQuery(clazz);
    }

    private <T> T executeSingleQuery(Class<T> clazz) {
        String sql = buildSQL();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToDao(resultSet, clazz);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private <T> List<T> executeListQuery(Class<T> clazz) {
        String sql = buildSQL();
        List<T> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapToDao(resultSet, clazz));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private void setParameters(PreparedStatement statement) throws SQLException {
        if (conditions != null) {
            for (int i = 0; i < conditions.size(); i++) {
                statement.setObject(i + 1, conditions.get(i).getValue());
            }
        }
    }

    private <T> T mapToDao(ResultSet resultSet, Class<T> daoClass) throws SQLException {
        try {
            T daoInstance = daoClass.getDeclaredConstructor().newInstance();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            Map<String, Field> daoFieldsMap = getAllFieldsMap(daoClass);

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);

                if (columnValue == null) {
                    continue;
                }

                Field matchingField = findMatchingField(columnName, daoFieldsMap);

                if (matchingField != null) {
                    matchingField.setAccessible(true);

                    columnValue = convertType(columnValue, matchingField.getType());
                    matchingField.set(daoInstance, columnValue);
                } else {
                    System.out.println("Warning: No matching field found in "
                            + daoClass.getSimpleName() + " for column '" + columnName + "'");
                }
            }

            return daoInstance;

        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to map ResultSet to " + daoClass.getSimpleName(), e);
        }
    }

    private Map<String, Field> getAllFieldsMap(Class<?> clazz) {
        Map<String, Field> fieldsMap = new HashMap<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                fieldsMap.put(field.getName(), field);

                if (field.getName().contains("_")) {
                    String camelCaseName = toCamelCase(field.getName());
                    fieldsMap.put(camelCaseName, field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return fieldsMap;
    }

    private Field findMatchingField(String columnName, Map<String, Field> daoFieldsMap) {
        if (daoFieldsMap.containsKey(columnName)) {
            return daoFieldsMap.get(columnName);
        }

        String camelCaseName = toCamelCase(columnName);
        if (daoFieldsMap.containsKey(camelCaseName)) {
            return daoFieldsMap.get(camelCaseName);
        }

        columnName = columnName.toLowerCase();
        for (String fieldName : daoFieldsMap.keySet()) {
            if (fieldName.toLowerCase().equals(columnName)) {
                return daoFieldsMap.get(fieldName);
            }
        }

        String normalizedColumnName = normalizeName(columnName);
        for (String fieldName : daoFieldsMap.keySet()) {
            if (normalizeName(fieldName).equals(normalizedColumnName)) {
                return daoFieldsMap.get(fieldName);
            }
        }

        return null;
    }

    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (value instanceof Number) {
            Number number = (Number) value;

            if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            } else if (targetType == Float.class || targetType == float.class) {
                return number.floatValue();
            } else if (targetType == String.class) {
                return number.toString();
            }
        }

        if (targetType == String.class) {
            return value.toString();
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            } else if (value instanceof String) {
                String strVal = ((String) value).toLowerCase();
                return strVal.equals("true") || strVal.equals("1") || strVal.equals("y");
            }
        }

        return value;
    }

    private String normalizeName(String name) {
        if (name == null) return "";

        name = name.toLowerCase();

        if (name.contains(".")) {
            name = name.substring(name.indexOf(".") + 1);
        }

        name = name.replace("_id", "")
                .replace("_name", "")
                .replace("_number", "")
                .replace("_date", "");

        return name;
    }

    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        if (snakeCase.contains(".")) {
            snakeCase = snakeCase.substring(snakeCase.indexOf(".") + 1);
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (int i = 0; i < snakeCase.length(); i++) {
            char currentChar = snakeCase.charAt(i);

            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }

        return result.toString();
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();

        switch (requestType) {
            case SELECT_AND:
                sql.append("SELECT * FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < conditions.size(); i++) {
                        if (i > 0) sql.append(" AND ");
                        sql.append(conditions.get(i).getColumn()).append(" ")
                                .append(conditions.get(i).getOperator()).append(" ?");
                    }
                }
                break;
            case SELECT_OR:
                sql.append("SELECT * FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < conditions.size(); i++) {
                        if (i > 0) sql.append(" OR ");
                        sql.append(conditions.get(i).getColumn()).append(" ")
                                .append(conditions.get(i).getOperator()).append(" ?");
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Request type " + requestType + " not implemented yet");
        }

        return sql.toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
    }

    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }

    public static class DBRequestBuilder {
        private RequestType requestType;
        private String table;
        private List<Condition> conditions = new ArrayList<>();
        private Class<?> extractAsClass;

        public DBRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public DBRequestBuilder table(String table) {
            this.table = table;
            return this;
        }

        public <T> T extractAs(Class<T> clazz) {
            this.extractAsClass = clazz;
            DBRequest request = DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .build();
            return request.extractAs(clazz);
        }

        public <T> List<T> extractAsList(Class<T> clazz) {
            this.extractAsClass = clazz;
            DBRequest request = DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .build();
            return request.extractAsList(clazz);
        }
    }
}