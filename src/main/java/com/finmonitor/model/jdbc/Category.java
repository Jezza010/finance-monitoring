package com.finmonitor.model.jdbc;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Category  implements Serializable {

    // -- Назначение: Описывает категорию ?!!

    private Integer id;
    private String categoryName;
    private boolean removed;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return removed == category.removed && Objects.equals(categoryName, category.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, removed);
    }
}
