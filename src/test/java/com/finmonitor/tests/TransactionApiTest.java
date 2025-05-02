package com.finmonitor.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .body("comment", equalTo("Обновлено через API"))
                .body("amount", equalTo(9999.99f));
    }

    @Test
    void testDeleteTransaction() {
        given()
                .when()
                .delete(BASE_URL + "delete_transaction?id=" + transactionId)
                .then()
                .log().body()
                .statusCode(200)
                .body("deleted", equalTo(true));
    }

    @Test void testGetTransactionsCount() {
        get(BASE_URL + "transactions_count?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetDebetCount() {
        get(BASE_URL + "debet_count?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetCreditCount() {
        get(BASE_URL + "credit_count?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetSumIncome() {
        get(BASE_URL + "sum_income?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetSumOutcome() {
        get(BASE_URL + "sum_outcome?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetCompletedTransactions() {
        get(BASE_URL + "completed_transactions?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testGetCancelledTransactions() {
        get(BASE_URL + "cancelled_transactions?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testBankIncomeStats() {
        get(BASE_URL + "bank_income_stats?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testBankOutcomeStats() {
        get(BASE_URL + "bank_outcome_stats?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }

    @Test void testCategoryStats() {
        get(BASE_URL + "category_stats?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
}
