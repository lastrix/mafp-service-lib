package org.lastrix.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rest<T> {
    private boolean success;
    private List<String> errors;
    private List<T> data;
    private Pagination pagination;

    public Rest(boolean success, List<String> errors) {
        this.success = success;
        this.errors = errors;
    }

    public Rest(List<T> data) {
        this.success = true;
        this.data = data;
    }

    public Rest(List<T> data, Pagination pagination) {
        this.success = true;
        this.data = data;
        this.pagination = pagination;
    }

    public static Rest<Boolean> ok() {
        return of(true);
    }

    public static Rest<Boolean> fail() {
        return of(false);
    }

    public static <T> ResponseEntity<Rest<T>> error(String error, HttpStatus status) {
        return new ResponseEntity<>(new Rest<>(false, Collections.singletonList(error)), status);
    }

    public static <T> Rest<T> of(T result) {
        return new Rest<>(Collections.singletonList(result));
    }

    public static <E, D> Rest<D> of(E result, EntityMapper<E, D> mapper) {
        return of(mapper.toDto(result));
    }

    public static <T> Rest<T> of(Collection<T> result) {
        return new Rest<>(new ArrayList<>(result));
    }

    public static <E, D> Rest<D> of(Collection<E> result, EntityMapper<E, D> mapper) {
        return of(result.stream().map(mapper::toDto).collect(Collectors.toList()));
    }

    public static <T> Rest<T> of(List<T> result, Pagination pagination) {
        var p = new Pagination(pagination);
        return new Rest<>(result, p);
    }

    public static <T> Rest<T> of(Slice<T> result, Pagination pagination) {
        var p = new Pagination(pagination);
        if (result instanceof Page) {
            var page = (Page<T>) result;
            p.setPageCount(page.getTotalPages());
            p.setTotalCount(page.getTotalElements());
        }
        return new Rest<>(result.getContent(), p);
    }

    public static <E, D> Rest<D> of(Slice<E> result, Pagination pagination, EntityMapper<E, D> mapper) {
        var p = new Pagination(pagination);
        if (result instanceof Page) {
            var page = (Page<E>) result;
            p.setPageCount(page.getTotalPages());
            p.setTotalCount(page.getTotalElements());
        }
        var items = result.getContent()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return new Rest<>(items, p);
    }
}
