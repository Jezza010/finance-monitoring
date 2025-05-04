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

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080/api";
        var user = """
                        {"username":"testUser","password":"testUserPassword"}
                   """;

        given() // try to create user
                .body(user)
                .when()
                .post("/auth/register")
                .detailedCookie("session");;

        sessionCookie = given()
                .when()
                .body(user)
                .post("/auth/login")
                .detailedCookie("session");

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
                .cookie(sessionCookie)
                .contentType(ContentType.JSON)
                .body(jsonInputString)
                .post("/transaction")
                .then()
                .statusCode(200)
                .extract()
                .response();

        transactionId = response.jsonPath().getLong("id");
    }

    @Test
    void testAddAndGetTransactions() {
        given()
                .cookie(sessionCookie)
                .when()
                .get("/transaction")
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
                .cookie(sessionCookie)
                .contentType(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put("/update_transaction")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testDeleteTransaction() {
        given()
                .cookie(sessionCookie)
                .when()
                .delete("/delete_transaction?id=" + transactionId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
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
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/create_category?category=foo")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"W", "M", "Q", "Y"})
    void testGetTransactionsCount(String period) {
        given()
                .cookie(sessionCookie) // имя и значение куки
                .when()
                .get("/transactions_count?period=M")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body(not(empty()));
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
