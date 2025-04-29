package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatus extends Identity<Integer> {

    // Назначение: Описывает возможные статусы операций.

    private Integer id;
    private String transactionStatusName;
    private boolean isImmutable;
    private boolean isCompleted;
    private boolean isSuccessful;
    private boolean isDeleted;
    private boolean removed;

    public TransactionStatus(Integer id, Integer id1, String transactionStatusName, boolean isImmutable, boolean isCompleted, boolean isSuccessful, boolean isDeleted, boolean removed) {
        super(id);
        this.id = id1;
        this.transactionStatusName = transactionStatusName;
        this.isImmutable = isImmutable;
        this.isCompleted = isCompleted;
        this.isSuccessful = isSuccessful;
        this.isDeleted = isDeleted;
        this.removed = removed;
    }

    @Override
    protected void validateId() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionStatus that = (TransactionStatus) o;
        return isDeleted == that.isDeleted && removed == that.removed && Objects.equals(transactionStatusName, that.transactionStatusName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionStatusName, isDeleted, removed);
    }
}
