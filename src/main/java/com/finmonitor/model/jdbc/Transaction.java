package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction  implements Serializable {

    public Long id;
    public Date transaction_datetime;
    public double sum;
    public String comment;
    public String senderBank;
    public String senderAccountNumber;
    public String receiverINN;
    public String receiverBank;
    public String receiverAccountNumber;
    public String receiverPhone;
    public String personTypeId;
    public String transactionTypeId;
    public String transactionStatusId;
    public String categoryId;
    public Long userId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(sum, that.sum) == 0 &&
                Objects.equals(transaction_datetime, that.transaction_datetime) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(senderBank, that.senderBank) &&
                Objects.equals(senderAccountNumber, that.senderAccountNumber) &&
                Objects.equals(receiverINN, that.receiverINN)
                && Objects.equals(receiverBank, that.receiverBank)
                && Objects.equals(receiverAccountNumber, that.receiverAccountNumber)
                && Objects.equals(receiverPhone, that.receiverPhone)
                && Objects.equals(personTypeId, that.personTypeId)
                && Objects.equals(transactionTypeId, that.transactionTypeId)
                && Objects.equals(transactionStatusId, that.transactionStatusId)
                && Objects.equals(categoryId, that.categoryId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                transaction_datetime, sum,
                comment,
                senderBank, senderAccountNumber,
                receiverINN, receiverBank, receiverAccountNumber, receiverPhone,
                personTypeId, transactionTypeId, transactionStatusId, categoryId, userId);
    }
}