package org.lastrix.http.client.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lastrix.rest.Rest;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractRestService {
    protected final <T> T singleResult(Supplier<Rest<T>> restSupplier) {
        var list = allResults(restSupplier);
        if (list.size() != 1) {
            throw new IllegalStateException("Single result expected");
        }
        return list.get(0);
    }

    protected final <T> List<T> allResults(Supplier<Rest<T>> restSupplier) {
        var r = restSupplier.get();
        if (!r.isSuccess()) {
            handleErrors(r);
        }
        return r.getData();
    }


    private <T> void handleErrors(Rest<T> rest) {
        if (rest.getErrors() == null || rest.getErrors().isEmpty()) {
            throw new IllegalStateException("No error supplied");
        }
        rest.getErrors().forEach(e -> log.error("Got error from remote:\r\n{}", e));
        throw new IllegalStateException(StringUtils.join(rest.getErrors(), System.lineSeparator()));
    }

}
