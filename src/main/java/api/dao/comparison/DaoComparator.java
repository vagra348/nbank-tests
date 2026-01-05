package api.dao.comparison;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class DaoComparator {

    private final DaoComparisonConfigLoader configLoader;
    private double epsilon = 0.01;

    public DaoComparator() {
        this.configLoader = new DaoComparisonConfigLoader("dao-comparison.properties");
    }

    public DaoComparator withEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public void compare(Object apiResponse, Object dao) {
        DaoComparisonConfigLoader.DaoComparisonRule rule = configLoader.getRuleFor(apiResponse.getClass());

        if (rule == null) {
            throw new RuntimeException("No comparison rule found for " + apiResponse.getClass().getSimpleName());
        }

        Map<String, String> fieldMappings = rule.getFieldMappings();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiFieldName = mapping.getKey();
            String daoFieldName = mapping.getValue();

            Object apiValue = getFieldValue(apiResponse, apiFieldName);
            Object daoValue = getFieldValue(dao, daoFieldName);

            if (!areValuesEqual(apiValue, daoValue)) {
                throw new AssertionError(String.format(
                        "Field mismatch for %s: API=%s, DAO=%s",
                        apiFieldName, apiValue, daoValue));
            }
        }
    }

    private boolean areValuesEqual(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }

        if (value1 instanceof Number && value2 instanceof Number) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return Math.abs(num1 - num2) <= epsilon;
        }

        if (value1 instanceof BigDecimal && value2 instanceof BigDecimal) {
            BigDecimal bd1 = (BigDecimal) value1;
            BigDecimal bd2 = (BigDecimal) value2;
            int scale = getScaleFromEpsilon(epsilon);
            BigDecimal bd1Rounded = bd1.setScale(scale, RoundingMode.HALF_UP);
            BigDecimal bd2Rounded = bd2.setScale(scale, RoundingMode.HALF_UP);
            return bd1Rounded.compareTo(bd2Rounded) == 0;
        }

        try {
            double num1 = Double.parseDouble(String.valueOf(value1));
            double num2 = Double.parseDouble(String.valueOf(value2));
            return Math.abs(num1 - num2) <= epsilon;
        } catch (NumberFormatException e) {
            return String.valueOf(value1).equals(String.valueOf(value2));
        }
    }

    private int getScaleFromEpsilon(double epsilon) {
        if (epsilon <= 0.001) return 3;
        if (epsilon <= 0.01) return 2;
        if (epsilon <= 0.1) return 1;
        return 0;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }
}