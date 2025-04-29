package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionType  implements Serializable {

    // Назначение: Описывает возможные типы операций.

    private Integer id;
    private String transactionTypeName;
    private String sign_operator;
    private boolean removed;

    public TransactionType(Integer id, Integer id1, String transactionTypeName, String sign_operator, boolean removed) {
        this.id = id;
        this.transactionTypeName = transactionTypeName;
        this.sign_operator = sign_operator;
        this.removed = removed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionType that = (TransactionType) o;
        return removed == that.removed && Objects.equals(transactionTypeName, that.transactionTypeName) && Objects.equals(sign_operator, that.sign_operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionTypeName, sign_operator, removed);
    }
}
