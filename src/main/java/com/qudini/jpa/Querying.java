package com.qudini.jpa;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

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

    /**
     * Get a single result, which is either missing or present. The caller is declaring that more than 1 result should
     * not happen, in which case an {@link MoreThanOneResultException} is thrown.
     */
    @Nonnull
    public static <A> Optional<A> zeroOrOne(TypedQuery<A> x) {
        List<A> results = x.setMaxResults(2).getResultList();
        final Optional<A> result;
        switch (results.size()) {
            case 0:
                result = Optional.empty();
                break;
            case 1:
                result = Optional.of(results.get(0));
                break;
            default:
                throw new MoreThanOneResultException(x);
        }
        return result;
    }
}