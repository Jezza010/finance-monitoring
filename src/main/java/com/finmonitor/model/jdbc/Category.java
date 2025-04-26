package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Category extends Identity<Integer> {

    // -- Назначение: Описывает категорию ?!!

    private Integer id;
    private String categoryName;
    private boolean removed;

    @Override
    protected void validateId() {}

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
