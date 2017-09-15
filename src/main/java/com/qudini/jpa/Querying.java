package com.qudini.jpa;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * JPA's criteria API is a good alternative to JPA expressions, but is spoiled by too much boilerplace to set a
 * query up. These methods fix that.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Querying {

    /**
     * Example usage:
     * <pre><code>
     * long count =
     *         query(
     *                 theEntityManager,
     *                 models.AuditEvent.class,
     *                 Long.class,
     *                 (builder, root, resultQuery) -> {
     *
     *                     final Predicate datePredicates = builder.and(
     *                             builder.greaterThan(root.get("timestamp"), startDateTime),
     *                             builder.lessThanOrEqualTo(root.get("timestamp"), endDateTime)
     *                     );
     *
     *                     return resultQuery.select(builder.count(root)).where(
     *                             userId > 0
     *                                     ? builder.and(builder.equal(root.get("userId"), userId), datePredicates)
     *                                     : datePredicates
     *                     );
     *                 }
     *         ).getSingleResult();
     * </code></pre>
     */
    @Nonnull
    public static <RootModel, Result> TypedQuery<Result> query(
            final EntityManager entityManager,
            final Class<RootModel> rootModel,
            final Class<Result> resultModel,
            final QueryAction<RootModel, Result> query
    ) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Result> resultQuery = builder.createQuery(resultModel);
        final Root<RootModel> root = resultQuery.from(rootModel);

        return entityManager.createQuery(query.query(builder, root, resultQuery));
    }

    /**
     * Example usage:
     * <pre><code>
     * final List&lt;PassphraseHashingAlgorithm&gt; results =
     *         query(
     *                 theEntityManager,
     *                 PassphraseHashingAlgorithm.class,
     *                 (builder, root, query) -> query
     *                         .orderBy(
     *                                 builder.desc(root.get("processorCost")),
     *                                 builder.desc(root.get("processorCost")),
     *                                 builder.desc(root.get("memoryCost")),
     *                                 builder.desc(root.get("parallelisationParameter")),
     *                                 builder.desc(root.get("derivedKeyLength"))
     *                         )
     *                         .where(builder.equal(
     *                                 root.get("algorithmName"),
     *                                 PassphraseHashingAlgorithmName.SCRYPT
     *                         ))
     *
     *         ).setMaxResults(2).setResultList();
     * </code></pre>
     */
    @Nonnull
    public static <A> TypedQuery<A> query(
            final EntityManager entityManager,
            final Class<A> model,
            final QueryAction<A, A> query
    ) {
        return query(entityManager, model, model, query);
    }

    @FunctionalInterface
    @ParametersAreNonnullByDefault
    @CheckReturnValue
    public interface QueryAction<RootModel, Result> {

        @Nonnull
        CriteriaQuery<Result> query(
                final CriteriaBuilder builder,
                final Root<RootModel> root,
                final CriteriaQuery<Result> resultQuery
        );
    }
}