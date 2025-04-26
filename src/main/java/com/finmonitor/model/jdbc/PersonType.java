package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PersonType extends Identity<Integer> {

    // -- Назначение: Описывает юридический статус владельца транзакции.

    private Integer id;
    private String personTypeName;

    @Override
    protected void validateId() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonType that = (PersonType) o;
        return Objects.equals(personTypeName, that.personTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(personTypeName);
    }
}
