public class SimpleBinomialTree {

    private final double delta;
    private final double optionPrice;
    private final double presentPortValue;
    private final double expectedValue;

    /**
     * Implements a single-step binomial tree model for option pricing.
     * <p>
     * This model is inspired by:
     * - Wilmott, P. (2013). *Paul Wilmott on Quantitative Finance*, Chapter 15.
     * - [YouTube Video by Perfiliev Financial Training](https://www.youtube.com/watch?v=eA5AtTx3rRI)
     * <p>
     * The binomial tree method provides a discrete-time framework for modeling
     * the underlying asset's price dynamics and valuing financial derivatives.
     *
     * @param initialPrice  Initial asset price.
     * @param strikePrice   Strike price of the option.
     * @param probabilityUp The probability of the asset's value increasing in a single step.
     * @param upFactor      Upward movement factor.
     * @param downFactor    Downward movement factor.
     * @param interestRate  Risk-free interest rate per period (e.g., 0.05 for 5%).
     * @param isCall        True for Call option, False for Put option.
     */
    public SimpleBinomialTree(double initialPrice, double strikePrice, double probabilityUp, double upFactor, double downFactor, double interestRate, boolean isCall) {

        if (probabilityUp < 0 || probabilityUp > 1) {
            throw new IllegalArgumentException("Probability out of bounds; must be between 0 and 1");
        }

        if (upFactor < downFactor) {
            throw new IllegalArgumentException("The upper factor must be greater than the lower factor");
        }

        double probabilityDown = 1 - probabilityUp;

        double priceUp = initialPrice * upFactor;
        double priceDown = initialPrice * downFactor;

        double payoffUp = isCall ? Math.max(priceUp - strikePrice, 0) : Math.max(strikePrice - priceUp, 0);
        double payoffDown = isCall ? Math.max(priceDown - strikePrice, 0) : Math.max(strikePrice - priceDown, 0);

        expectedValue = (probabilityUp * payoffUp + probabilityDown * payoffDown) / (1 + interestRate);

        delta = calculateDelta(payoffUp, payoffDown, priceUp, priceDown);

        presentPortValue = portfolioValue(delta, payoffUp, priceUp) / (1 + interestRate);

        optionPrice = calculateOptionPrice(presentPortValue, delta, initialPrice);
    }

    //calculating the hedging ratio
    private static double calculateDelta(double payoffUp, double payoffDown, double priceUp, double priceDown) {
        return (payoffUp - payoffDown) / (priceUp - priceDown);
    }

    private static double portfolioValue(double delta, double value, double price) {
        return delta * price - value;
    }

    private static double calculateOptionPrice(double presentPortValue, double delta, double initialPrice) {
        return delta * initialPrice - presentPortValue;
    }

    // Getter methods
    public double getOptionPrice() {
        return optionPrice;
    }

    public double getDelta() {
        return delta;
    }

    public double getPresentPortValue() {
        return presentPortValue;
    }

    public double getExpectedValue() {
        return expectedValue;
    }
}
