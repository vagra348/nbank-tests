package api.models.comparison;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelComparator {

    public static <A, B> ComparisonResult compareFields(A request, B response, Map<String, String> fieldMappings) {
        return compareFields(request, response, fieldMappings, 0.01);
    }

    public static <A, B> ComparisonResult compareFields(A request, B response, Map<String, String> fieldMappings, double epsilon) {
        List<ComparisonResult.Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object value1 = getFieldValue(request, requestField);
            Object value2 = getFieldValue(response, responseField);

            if (!areValuesEqual(value1, value2, epsilon)) {
                mismatches.add(new ComparisonResult.Mismatch(
                        requestField + " -> " + responseField,
                        value1,
                        value2
                ));
            }
        }

        return new ComparisonResult(mismatches);
    }

    private static boolean areValuesEqual(Object value1, Object value2, double epsilon) {
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

    private static int getScaleFromEpsilon(double epsilon) {
        if (epsilon <= 0.001) return 3;
        if (epsilon <= 0.01) return 2;
        if (epsilon <= 0.1) return 1;
        return 0;
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName + " in class " + obj.getClass().getName());
    }

    public static class ComparisonResult {
        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        public List<Mismatch> getMismatches() {
            return mismatches;
        }

        @Override
        public String toString() {
            if (isSuccess()) {
                return "All fields match";
            }
            StringBuilder sb = new StringBuilder("Mismatched fields:\n");
            for (Mismatch m : mismatches) {
                sb.append("- ").append(m.fieldName)
                        .append(": expected=").append(m.expected)
                        .append(", actual=").append(m.actual).append("\n");
            }
            return sb.toString();
        }

        public static class Mismatch {
            public final String fieldName;
            public final Object expected;
            public final Object actual;

            public Mismatch(String fieldName, Object expected, Object actual) {
                this.fieldName = fieldName;
                this.expected = expected;
                this.actual = actual;
            }
        }
    }
}