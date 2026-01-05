package base;

import api.configs.Config;
import common.annotations.MockWith;
import common.extensions.TimingExtension;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@ExtendWith(TimingExtension.class)
public class WireMockTest extends BaseTest {

    protected static final String WIREMOCK_URL = Config.getProperty("wiremock.url");
    protected static final String MOCKS_PATH = Config.getProperty("mocks.path");

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = WIREMOCK_URL;
        RestAssured.port = 8085;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @BeforeEach
    public void setupWireMock() {
        resetWireMock();

        loadMocksFromAnnotation();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanupWireMock() {
        resetWireMock();
    }

    private void loadMocksFromAnnotation() {
        MockWith mockAnnotation = this.getClass().getAnnotation(MockWith.class);

        if (mockAnnotation != null) {
            for (String mockFile : mockAnnotation.value()) {
                loadMock(mockFile);
            }
        }
    }

    protected void loadMock(String fileName) {
        try {
            String fullPath = MOCKS_PATH + fileName;

            if (!Files.exists(Paths.get(fullPath))) {
                System.err.println("Mock file not found: " + fullPath);
                return;
            }

            String jsonContent = new String(Files.readAllBytes(Paths.get(fullPath)));

            io.restassured.RestAssured.given()
                    .baseUri(WIREMOCK_URL)
                    .contentType("application/json")
                    .body(jsonContent)
                    .post("/__admin/mappings")
                    .then()
                    .statusCode(201);

        } catch (IOException e) {
            System.err.println("Failed to load mock: " + fileName);
            e.printStackTrace();
        }
    }

    protected void resetWireMock() {
        try {
            io.restassured.RestAssured.given()
                    .baseUri(WIREMOCK_URL)
                    .post("/__admin/mappings/reset")
                    .then()
                    .statusCode(200);
        } catch (Exception e) {
            System.err.println("Failed to reset WireMock: " + e.getMessage());
        }
    }

}