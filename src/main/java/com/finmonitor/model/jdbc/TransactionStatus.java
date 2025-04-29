package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatus  implements Serializable {

    // Назначение: Описывает возможные статусы операций.

    private Integer id;
    private String transactionStatusName;
    private boolean isImmutable;
    private boolean isCompleted;
    private boolean isSuccessful;
    private boolean isDeleted;
    private boolean removed;

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
