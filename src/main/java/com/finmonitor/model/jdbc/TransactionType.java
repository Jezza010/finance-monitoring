package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionType extends Identity<Integer> {

    // Назначение: Описывает возможные типы операций.

    private Integer id;
    private String transactionTypeName;
    private String sign_operator;
    private boolean removed;

    public TransactionType(Integer id, Integer id1, String transactionTypeName, String sign_operator, boolean removed) {
        super(id);
        this.id = id1;
        this.transactionTypeName = transactionTypeName;
        this.sign_operator = sign_operator;
        this.removed = removed;
    }

    @Override
    protected void validateId() {}

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
