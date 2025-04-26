package com.finmonitor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
@NoArgsConstructor(force = true)
public abstract class Identity<K> implements Serializable {
    protected final K id;

    public Identity(K id) {
        this.id = id;
        validateId();
    }

    protected abstract void validateId();
}
