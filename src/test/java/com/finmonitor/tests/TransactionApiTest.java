package com.finmonitor.tests;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TransactionApiTest {

    private static final String BASE_URL = "http://localhost:8080/api/";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    void testAddAndGetTransactions() {
        String jsonInputString = "{"
                + "\"personType\": \"Физическое лицо\","
                + "\"transactionType\": \"Поступление\","
                + "\"dateTime\": \"2025-04-23T14:00:00\","
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

        given()
                .contentType(ContentType.JSON)
                .body(jsonInputString)
                .when()
                .post(BASE_URL + "transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dateTime", notNullValue());

        given()
                .when()
                .get(BASE_URL + "transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()));
    }

    @Test
    void testGetTransactionsCount() {
        given()
                .when()
                .get(BASE_URL + "transactions_count?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetDebetCount() {
        given()
                .when()
                .get(BASE_URL + "debet_count?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetCreditCount() {
        given()
                .when()
                .get(BASE_URL + "credit_count?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetSumIncome() {
        given()
                .when()
                .get(BASE_URL + "sum_income?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetSumOutcome() {
        given()
                .when()
                .get(BASE_URL + "sum_outcome?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetCompletedTransactions() {
        given()
                .when()
                .get(BASE_URL + "completed_transactions?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testGetCancelledTransactions() {
        given()
                .when()
                .get(BASE_URL + "cancelled_transactions?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testBankIncomeStats() {
        given()
                .when()
                .get(BASE_URL + "bank_income_stats?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testBankOutcomeStats() {
        given()
                .when()
                .get(BASE_URL + "bank_outcome_stats?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test
    void testCategoryStats() {
        given()
                .when()
                .get(BASE_URL + "category_stats?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
}
