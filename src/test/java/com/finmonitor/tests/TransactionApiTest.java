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
    void testGetAllTransactions() {
        given()
                .when()
                .get(BASE_URL + "transaction")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()));
    }

    @Test
    void testAddTransaction() {
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
    void testGetSumIncome() {
        given()
                .when()
                .get(BASE_URL + "sum_income?period=M")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
    }
}
