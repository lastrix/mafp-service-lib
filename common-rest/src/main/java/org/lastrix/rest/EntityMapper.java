package org.lastrix.rest;

public interface EntityMapper<E, D> {
    E fromDto(D dto);

    D toDto(E entity);
}
