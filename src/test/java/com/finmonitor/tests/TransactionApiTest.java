package com.finmonitor.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TransactionApiTest {

    private static final String BASE_URL = "http://localhost:8080/api/";
    private static Long transactionId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @BeforeEach
    void createTestTransaction() {
        String jsonInputString = "{"
                + "\"personType\": \"Физическое лицо\","
                + "\"transactionType\": \"Поступление\","
                + "\"dateTime\": \"24.03.2025\","
                + "\"comment\": \"Оплата товара\","
                + "\"amount\": 5000.75,"
                + "\"status\": \"Новая\","
                + "\"senderBank\": \"Сбербанк\","
                + "\"receiverBank\": \"Тинькофф\","
                + "\"receiverINN\": \"12345678901\","
                + "\"receiverAccount\": \"40817810000001234567\","
                + "\"category\": \"Покупка\","
                + "\"phone\": \"+79261234567\""
                + "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(jsonInputString)
                .post(BASE_URL + "transaction")
                .then()
                .statusCode(200)
                .extract()
                .response();

        transactionId = response.jsonPath().getLong("id");
    }

    @Test
    void testAddAndGetTransactions() {
        given()
                .when()
                .get(BASE_URL + "transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()));
    }

    @Test
    void testUpdateTransaction() {
        String updatedJson = "{"
                + "\"id\": " + transactionId + ","
                + "\"personType\": \"Юридическое лицо\","
                + "\"transactionType\": \"Списание\","
                + "\"dateTime\": \"25.03.2025\","
                + "\"comment\": \"Обновлено через API\","
                + "\"amount\": 9999.99,"
                + "\"status\": \"Новая\","
                + "\"senderBank\": \"Газпромбанк\","
                + "\"receiverBank\": \"Райффайзенбанк\","
                + "\"receiverINN\": \"98765432109\","
                + "\"receiverAccount\": \"40817810000007654321\","
                + "\"category\": \"Оплата услуг\","
                + "\"phone\": \"+79001234567\""
                + "}";

        given()
                .contentType(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(BASE_URL + "update_transaction/" + transactionId)
                .then()
                .log().body()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testDeleteTransaction() {
        given()
                .when()
                .delete(BASE_URL + "delete_transaction?id=" + transactionId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testExport() {
        given()
                .when()
                .get(BASE_URL + "export")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    void testCreateCategory() {
        get(BASE_URL + "create_category?category=foo")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetTransactionsCount(String period) {
        get(BASE_URL + "transactions_count?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetDebetCount(String period) {
        get(BASE_URL + "debet_count?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCreditCount(String period) {
        get(BASE_URL + "credit_count?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetSumIncome(String period) {
        get(BASE_URL + "sum_income?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetSumOutcome(String period) {
        get(BASE_URL + "sum_outcome?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCompletedTransactions(String period) {
        get(BASE_URL + "completed_transactions?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetCancelledTransactions(String period) {
        get(BASE_URL + "cancelled_transactions?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testBankIncomeStats(String period) {
        get(BASE_URL + "bank_income_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testBankOutcomeStats(String period) {
        get(BASE_URL + "bank_outcome_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testCategoryStats(String period) {
        get(BASE_URL + "category_stats?period=" + period)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
}
