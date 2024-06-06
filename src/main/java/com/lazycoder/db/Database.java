package com.lazycoder.db;

import java.lang.annotation.Annotation;
// ORMManager.java
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.lazycoder.db.annotation.Column;
import com.lazycoder.db.annotation.Entity;
import com.lazycoder.db.annotation.Id;

public class Database {
    private void createTableIfNotExists(Class<?> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("Class is not annotated with @Entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName();
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        List<String> columns = new ArrayList<>();
        String primaryKey = null;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name();
                String columnType = getColumnType(field.getType());

                if (field.isAnnotationPresent(Id.class)) {
                    primaryKey = columnName;
                }

                columns.add(columnName + " " + columnType);
            }
        }

        if (primaryKey != null) {
            columns.add("PRIMARY KEY (" + primaryKey + ")");
        }

        sql.append(String.join(", ", columns));
        sql.append(");");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.executeUpdate();
        }
    }

    private String getColumnType(Class<?> fieldType) {
        if (fieldType == int.class || fieldType == Integer.class) {
            return "INTEGER";
        } else if (fieldType == String.class) {
            return "TEXT";
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "REAL";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        }
        throw new RuntimeException("Unsupported field type: " + fieldType.getName());
    }

    public void save(Object obj) throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        if (!clazz.isAnnotationPresent((Class<? extends Annotation>) Entity.class)) {
            throw new RuntimeException("Class is not annotated with @Entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName();

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent((Class<? extends Annotation>) Column.class)) {
                Column column = field.getAnnotation(Column.class);
                sql.append(column.name()).append(",");
                values.append("?,");
                field.setAccessible(true);
                params.add(field.get(obj));
            }
        }

        sql.setLength(sql.length() - 1); // Remove trailing comma
        values.setLength(values.length() - 1); // Remove trailing comma
        sql.append(") ").append(values).append(")");

        try {
            Connection conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        } catch(Exception e) {
            if (e.getMessage().equals("[SQLITE_ERROR] SQL error or missing database (no such table: "+ tableName + ")")) {
                createTableIfNotExists(obj.getClass());
                save(obj);
            }
        }
    }

    public void delete(Object obj) throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("Class is not annotated with @Entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName();

        StringBuilder sql = new StringBuilder("DELETE FROM " + tableName + " WHERE ");
        List<Object> params = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent((Class<? extends Annotation>) Id.class)) {
                Id id = field.getAnnotation(Id.class);
                Column column = field.getAnnotation(Column.class);
                sql.append(column.name()).append(" = ?");
                field.setAccessible(true);
                params.add(field.get(obj));
                break;
            }
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    public <T> T findById(Class<T> clazz, Object id) throws SQLException, ReflectiveOperationException {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("Class is not annotated with @Entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
        Field idField = null;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                Column column = field.getAnnotation(Column.class);
                sql.append(column.name()).append(" = ?");
                break;
            }
        }

        if (idField == null) {
            throw new RuntimeException("No field annotated with @Id found");
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setObject(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Column.class)) {
                            Column column = field.getAnnotation(Column.class);
                            field.setAccessible(true);
                            field.set(obj, rs.getObject(column.name()));
                        }
                    }
                    return obj;
                }
            }
        }
        return null;
    }
}
