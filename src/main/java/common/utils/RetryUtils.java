package common.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T retry(Supplier<T> action, Predicate<T> condition, int maxAttempts, long delayMillis) {
        T result = null;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;
            result = action.get();
            if (condition.test(result)) {
                return result;
            }
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Retry failed after " + maxAttempts + " attempts!");
    }

    public static void retryVoidWithCheck(Runnable action, Supplier<Boolean> condition,
                                          int maxAttempts, long delayMillis) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            try {
                action.run();
                if (condition.get()) {
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }
        throw new RuntimeException("Retry failed after " + maxAttempts + " attempts!");
    }

}