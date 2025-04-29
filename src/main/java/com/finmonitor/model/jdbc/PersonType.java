package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PersonType  implements Serializable {

    // -- Назначение: Описывает юридический статус владельца транзакции.

    private Integer id;
    private String personTypeName;
    private boolean removed;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonType that = (PersonType) o;
        return removed == that.removed && Objects.equals(personTypeName, that.personTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personTypeName, removed);
    }
}
