public class MultiStepBinomialTree {
    private final double delta;
    private final double optionPrice;
    private final double presentPortValue;
    private final double expectedValue;
    private double[] optionValues;

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
     * @param steps         Number of steps
     */
    public MultiStepBinomialTree(double initialPrice, double strikePrice, double probabilityUp, double upFactor, double downFactor, double interestRate, boolean isCall, int steps) {

        if (probabilityUp < 0 || probabilityUp > 1) {
            throw new IllegalArgumentException("Probability out of bounds; must be between 0 and 1");
        }

        if (upFactor < downFactor) {
            throw new IllegalArgumentException("The upper factor must be greater than the lower factor");
        }

        if (steps <= 0){
            throw new IllegalArgumentException("Steps must be greater than zero");
        }

        System.out.println("Steps: " + steps);

        // Calculating risk-neutral probability
        double q = calculateRiskNeutralProbability(upFactor, downFactor, interestRate);

        // Establishing an array of possible stock prices at maturity
        // After n steps the stock can be i times and down (steps - i) times
        double[] stockPriceMaturity = new double[steps + 1];
        optionValues = new double[steps + 1];
        for (int i = 0; i <= steps; i++) {
            int ups = i;
            int downs = steps - i;
            stockPriceMaturity[i] = initialPrice * Math.pow(upFactor, ups) * Math.pow(downFactor, downs);

            // Initialising the possible options value at expiration
            optionValues[i] = calculateOptionPayoff(stockPriceMaturity[i], strikePrice, isCall);
        }

        System.out.println(stockPriceMaturity[steps]);

        // Backward induction
        for (int step = steps - 1; step >= 0; step--) {
            for (int i = 0; i <= step; i++) {

                SimpleBinomialTree tree = new SimpleBinomialTree(stockPriceMaturity[i],
                        strikePrice,
                        probabilityUp,
                        upFactor,
                        downFactor,
                        interestRate,
                        isCall
                );

                optionValues[i] = tree.getOptionPrice();
                System.out.println(optionValues[i]);
            }
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

    // Calculating the hedging ratio
    private static double calculateDelta(double payoffUp, double payoffDown, double priceUp, double priceDown) {
        return (payoffUp - payoffDown) / (priceUp - priceDown);
    }

    private static double portfolioValue(double delta, double value, double price) {
        return delta * price - value;
    }

    private static double calculateOptionPrice(double presentPortValue, double delta, double initialPrice) {
        return delta * initialPrice - presentPortValue;
    }

    private double calculateRiskNeutralProbability(double upFactor, double downFactor, double interestRate) {
        double q = ((1 + interestRate) - downFactor) / (upFactor - downFactor);
        if (q < 0 || q > 1) {
            throw new IllegalArgumentException("Invalid risk-neutral probability; check model parameters");
        }
        return q;
    }

    private double calculateOptionPayoff(double stockPrice, double strikePrice, boolean isCall) {
        return isCall ? Math.max(stockPrice - strikePrice, 0) : Math.max(strikePrice - stockPrice, 0);
    }

    // Getter methods
    public double getOptionPrice() {
        return optionValues[0];
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
