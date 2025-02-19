package com.qudini.jpa;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

public class MoreThanOneResultException extends PersistenceException {

    MoreThanOneResultException(TypedQuery<?> erroneousQuery) {
        super("only 0 or 1 results are accepted from query " + erroneousQuery);
    }
}
