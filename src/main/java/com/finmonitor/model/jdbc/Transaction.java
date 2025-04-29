package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends Identity<Long> {
    
    public Long id;
    public String transaction_datetime;
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
    public String statusId;
    public String categoryId;
    public String userId;

    @Override
    protected void validateId() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(sum, that.sum) == 0 && Objects.equals(transaction_datetime, that.transaction_datetime) && Objects.equals(comment, that.comment) && Objects.equals(senderBank, that.senderBank) && Objects.equals(senderAccountNumber, that.senderAccountNumber) && Objects.equals(receiverINN, that.receiverINN) && Objects.equals(receiverBank, that.receiverBank) && Objects.equals(receiverAccountNumber, that.receiverAccountNumber) && Objects.equals(receiverPhone, that.receiverPhone) && Objects.equals(personTypeId, that.personTypeId) && Objects.equals(transactionTypeId, that.transactionTypeId) && Objects.equals(statusId, that.statusId) && Objects.equals(categoryId, that.categoryId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transaction_datetime, sum, comment, senderBank, senderAccountNumber, receiverINN, receiverBank, receiverAccountNumber, receiverPhone, personTypeId, transactionTypeId, statusId, categoryId, userId);
    }
}