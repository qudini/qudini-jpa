package com.qudini.jpa;

import javax.persistence.TypedQuery;

public class MoreThanOneResultException extends javax.persistence.PersistenceException {

    MoreThanOneResultException(TypedQuery<?> erroneousQuery) {
        super("only 0 or 1 results are accepted from query " + erroneousQuery);
    }
}
