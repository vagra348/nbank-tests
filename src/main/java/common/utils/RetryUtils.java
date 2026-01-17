package common.utils;

import common.helpers.StepLogger;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T retry(
            String title,
            Supplier<T> action,
            Predicate<T> condition,
            int maxAttempts,
            long delayMillis) {

        T result = null;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            try {
                result = StepLogger.log("Attempt " + attempts + ": " + title, () -> action.get());

                if (condition.test(result)) {
                    return result;
                }
            } catch (Throwable e) {
                System.out.println("Exception " + e.getMessage());
            }

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("Retry failed after " + maxAttempts + " attempts!");
    }

    public static void retryVoidWithCheck(
            String title,
            Runnable action,
            Supplier<Boolean> condition,
            int maxAttempts,
            long delayMillis) {

        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            try {
                StepLogger.log("Attempt " + attempts + ": " + title, () -> {
                    action.run();
                    return null;
                });

                if (condition.get()) {
                    return;
                }
            } catch (Throwable e) {
                System.out.println("Exception " + e.getMessage());

                if (attempts == maxAttempts) {
                    throw new RuntimeException("Retry failed after " + maxAttempts + " attempts!", e);
                }
            }

            if (attempts < maxAttempts) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", e);
                }
            }
        }

        throw new RuntimeException("Retry failed after " + maxAttempts + " attempts!");
    }

}