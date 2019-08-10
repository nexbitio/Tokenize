package xyz.bowser65.tokenize;

/**
 * Interface your Account entity should implement to work with Tokenize
 * @author Bowser65
 * @since 10/08/19
 */
public interface IAccount {
    /**
     * Should return the Tokenize timestamp since when tokens are valid.
     * Use {@link Tokenize} to get it when you're creating an account or when you
     * need to invalidate tokens
     *
     * @return Tokenize timestamp since when tokens are valid.
     */
    long tokensValidSince();

    /**
     * @return Whether or not the user has MFA enabled on their account
     */
    boolean hasMfa();
}
