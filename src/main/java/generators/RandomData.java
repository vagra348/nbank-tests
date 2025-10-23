package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData(){}

    public static String qenerateUsername(){
        return RandomStringUtils.randomAlphanumeric(10);
    }

    public static String qeneratePassword(){
        return RandomStringUtils.randomAlphabetic(3).toLowerCase() +
                RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomNumeric(3) + "@#$";
    }

    public static String qenerateName(){
        return RandomStringUtils.randomAlphabetic(5) +
                " " + RandomStringUtils.randomAlphabetic(5);
    }
}
