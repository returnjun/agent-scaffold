package daoha.top.types.design.tree;

/**
 * Handles one step in a strategy chain.
 */
@FunctionalInterface
public interface StrategyHandler<T, D, R> {

    static <T, D, R> StrategyHandler<T, D, R> defaultHandler() {
        return (requestParameter, dynamicContext) -> null;
    }

    R apply(T requestParameter, D dynamicContext) throws Exception;
}
