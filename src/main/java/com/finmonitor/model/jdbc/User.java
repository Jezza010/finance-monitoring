package com.finmonitor.model.jdbc;


import com.finmonitor.model.Identity;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User extends Identity<Long> {

    private Long id;
    private String login;
    private String password;
    private boolean removed;

    @Override
    protected void validateId() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return removed == user.removed && Objects.equals(login, user.login) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, removed);
    }
}
