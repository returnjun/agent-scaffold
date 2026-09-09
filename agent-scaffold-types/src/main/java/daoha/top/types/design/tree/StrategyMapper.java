package daoha.top.types.design.tree;

/**
 * Selects the next handler in a strategy chain.
 */
public interface StrategyMapper<T, D, R> {

    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;
}
