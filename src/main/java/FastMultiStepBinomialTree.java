public class FastMultiStepBinomialTree {
    private final double optionPrice;

    /**
     * Constructs the binomial tree and computes the option price.
     *
     * @param initialPrice  Initial asset price.
     * @param strikePrice   Strike price of the option.
     * @param probabilityUp The probability of the asset's value increasing in a single step.
     * @param upFactor      Upward movement factor.
     * @param downFactor    Downward movement factor.
     * @param interestRate  Risk-free interest rate per period (e.g., 0.05 for 5%).
     * @param isCall        True for Call option, False for Put option.
     * @param steps         Number of steps in the tree.
     */
    public FastMultiStepBinomialTree(double initialPrice, double strikePrice, double probabilityUp,
                                 double upFactor, double downFactor, double interestRate,
                                 boolean isCall, int steps) {
        // Validate inputs.
        if (probabilityUp < 0 || probabilityUp > 1) {
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        }
        if (upFactor < downFactor) {
            throw new IllegalArgumentException("Up factor must be greater than down factor");
        }
        if (steps <= 0) {
            throw new IllegalArgumentException("Steps must be greater than zero");
        }
        if (upFactor <= interestRate + 1 || downFactor >= interestRate + 1) {
            throw new IllegalArgumentException("Property u > 1 + r > d > 0 must hold");
        }

        // Compute risk-neutral probability.
        double q = (1 + interestRate - downFactor) / (upFactor - downFactor);
        if (q <= 0 || q >= 1) {
            throw new IllegalArgumentException("Invalid risk-neutral probability; check model parameters");
        }

        // Allocate a 1D array for option values at maturity.
        double[] optionValues = new double[steps + 1];

        // Compute terminal payoffs.
        for (int i = 0; i <= steps; i++) {
            double stockPrice = initialPrice * Math.pow(upFactor, i) * Math.pow(downFactor, steps - i);
            optionValues[i] = isCall ? Math.max(stockPrice - strikePrice, 0)
                    : Math.max(strikePrice - stockPrice, 0);
        }

        // Backward induction (update in place).
        for (int step = steps - 1; step >= 0; step--) {
            for (int i = 0; i <= step; i++) {
                optionValues[i] = (q * optionValues[i + 1] + (1 - q) * optionValues[i])
                        / (1 + interestRate);
            }
        }

        // The final option price.
        optionPrice = optionValues[0];
    }

    public double getOptionPrice() {
        return optionPrice;
    }
}
