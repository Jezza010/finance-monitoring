package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction  implements Serializable {

    public Long id;
    public String dateTime;
    public double amount;
    public String comment;
    public String senderBank;
    public String senderAccountNumber;
    public String receiverINN;
    public String receiverBank;
    public String receiverAccountNumber;
    public String phone;
    public String personType;
    public String transactionType;
    public String status;
    public String category;
    public Long userId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(amount, that.amount) == 0 &&
                Objects.equals(dateTime, that.dateTime) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(senderBank, that.senderBank) &&
                Objects.equals(senderAccountNumber, that.senderAccountNumber) &&
                Objects.equals(receiverINN, that.receiverINN)
                && Objects.equals(receiverBank, that.receiverBank)
                && Objects.equals(receiverAccountNumber, that.receiverAccountNumber)
                && Objects.equals(phone, that.phone)
                && Objects.equals(personType, that.personType)
                && Objects.equals(transactionType, that.transactionType)
                && Objects.equals(status, that.status)
                && Objects.equals(category, that.category)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dateTime, amount,
                comment,
                senderBank, senderAccountNumber,
                receiverINN, receiverBank, receiverAccountNumber, phone,
                personType, transactionType, status, category, userId);
    }
}