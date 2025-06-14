package com.finmonitor.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TransactionApiTest {
    private static Cookie sessionCookie;
    private static Long transactionId;
    private static Cookie secondUserSessionCookie;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8086/api";
        var user = """
                        {"username":"testUser","password":"testUserPassword"}
                   """;

        // Register and login as first user
        given()
                .body(user)
                .when()
                .post("/auth/register")
                .detailedCookie("session");

        sessionCookie = given()
                .when()
                .body(user)
                .post("/auth/login")
                .detailedCookie("session");

        // Create a transaction for the first user
        String jsonInputString = "{"
                + "\"personType\": \"Физическое лицо\","
                + "\"transactionType\": \"Поступление\","
                + "\"dateTime\": \"2025-04-20T16:20\","
                + "\"comment\": \"Оплата товара\","
                + "\"amount\": 5000.75,"
                + "\"status\": \"Новая\","
                + "\"senderBank\": \"Сбербанк\","
                + "\"receiverBank\": \"Тинькофф\","
                + "\"receiverINN\": \"12345678901\","
                + "\"senderAccountNumber\": \"40817810000007654321\","
                + "\"receiverAccountNumber\": \"40817810000001234567\","
                + "\"category\": \"Покупка\","
                + "\"phone\": \"+79261234567\""
                + "}";

        Response response = given()
                .cookie(sessionCookie)
                .contentType(ContentType.JSON)
                .body(jsonInputString)
                .post("/transaction")
                .then()
                .statusCode(200)
                .extract()
                .response();

        transactionId = response.jsonPath().getLong("id");

        // Register and login as second user
        var secondUser = """
                        {"username":"testUser2","password":"testUserPassword2"}
                   """;

        given()
                .body(secondUser)
                .when()
                .post("/auth/register");

        secondUserSessionCookie = given()
                .when()
                .body(secondUser)
                .post("/auth/login")
                .detailedCookie("session");
    }

    @Test
    void testAddAndGetTransactions() {
        // First user should see their transaction
        given()
                .cookie(sessionCookie)
                .when()
                .get("/transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()));

        // Second user should not see first user's transaction
        given()
                .cookie(secondUserSessionCookie)
                .when()
                .get("/transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", empty());
    }

    @Test
    public void testFindByAllFilters() {
        given()
                .cookie(sessionCookie)
                .queryParam("from_bank", "Сбербанк")
                .queryParam("to_bank", "Тинькофф")
                .queryParam("from_date", "20.04.2025")
                .queryParam("to_date", "21.04.2025")
                .queryParam("status", "Новая")
                .queryParam("inn", "12345678901")
                .queryParam("amount_from", "5000.75")
                .queryParam("amount_to", "5000.75")
                .queryParam("type", "Поступление")
                .queryParam("category", "Покупка")
                .when()
                .get("/transaction")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void testUpdateTransaction() {
        String updatedJson = "{"
                + "\"id\": " + transactionId + ","
                + "\"personType\": \"Юридическое лицо\","
                + "\"transactionType\": \"Списание\","
                + "\"dateTime\": \"2025-04-20T16:30\","
                + "\"comment\": \"Обновлено через API\","
                + "\"amount\": 9999.99,"
                + "\"status\": \"Новая\","
                + "\"senderBank\": \"Газпромбанк\","
                + "\"receiverBank\": \"Райффайзенбанк\","
                + "\"receiverINN\": \"98765432109\","
                + "\"senderAccountNumber\": \"40817810000007654321\","
                + "\"receiverAccountNumber\": \"40817810000007654321\","
                + "\"category\": \"Покупка\","
                + "\"phone\": \"+79001234567\""
                + "}";

        // First user can update their transaction
        given()
                .cookie(sessionCookie)
                .contentType(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put("/update_transaction")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        // Second user cannot update first user's transaction
        given()
                .cookie(secondUserSessionCookie)
                .contentType(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put("/update_transaction")
                .then()
                .statusCode(500)
                .body("error", containsString("Transaction not found"));
    }

    @Test
    void testDeleteTransaction() {
        // First user can delete their transaction
        given()
                .cookie(sessionCookie)
                .when()
                .delete("/delete_transaction?id=" + transactionId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        // Second user cannot delete first user's transaction
        given()
                .cookie(secondUserSessionCookie)
                .when()
                .delete("/delete_transaction?id=" + transactionId)
                .then()
                .statusCode(500)
                .body("error", containsString("Transaction not found"));
    }

    @Test
    void testExport() {
        given()
                .cookie(sessionCookie)
                .when()
                .get("/export")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    void testCreateCategory() {
        // First user can create their category
        given()
                .cookie(sessionCookie)
                .when()
                .get("/create_category?category=foo")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true));

        // Second user can create the same category name (it's user-specific)
        given()
                .cookie(secondUserSessionCookie)
                .when()
                .get("/create_category?category=foo")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetTransactionsCount(String period) {
        // First user should see their transaction count
        given()
                .cookie(sessionCookie)
                .when()
                .get("/transactions_count?period=" + period)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));

        // Second user should see no transactions
        given()
                .cookie(secondUserSessionCookie)
                .when()
                .get("/transactions_count?period=" + period)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetDebetCount(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/debet_count?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCreditCount(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/credit_count?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetSumIncome(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/sum_income?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetSumOutcome(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/sum_outcome?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCompletedTransactions(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/completed_transactions?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCancelledTransactions(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/cancelled_transactions?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testBankIncomeStats(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/bank_income_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testBankOutcomeStats(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/bank_outcome_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testCategoryStats(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/category_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
}
