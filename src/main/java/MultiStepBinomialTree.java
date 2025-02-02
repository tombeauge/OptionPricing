import java.util.Arrays;
import java.util.List;

public class MultiStepBinomialTree {
    private final double optionPrice;
    private final double[][] optionValues;
    private final double[][] stockPrices;
    private double[][] stockPriceMaturity;

    private List<Double> optionPriceEvolution;

    /**
     * Implements a multi-step binomial tree model for option pricing.
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

        if (upFactor <= interestRate + 1 || downFactor >= interestRate + 1) {
            throw new IllegalArgumentException("Proprety u > 1 + r > d > 0 must hold");
        }

        // Calculating risk-neutral probability using discrete compounding
        double q = calculateRiskNeutralProbability(upFactor, downFactor, interestRate);

        // Allocate jagged arrays (each row i has i+1 elements).
        optionValues = new double[steps + 1][];
        stockPriceMaturity = new double[steps + 1][];
        for (int step = 0; step <= steps; step++) {
            optionValues[step] = new double[step + 1];
            stockPriceMaturity[step] = new double[step + 1];
        }

        stockPrices = new double[steps + 1][steps + 1];

        double[] upPowers = precomputePowers(upFactor, steps);
        double[] downPowers = precomputePowers(downFactor, steps);


        // Backward induction
        for (int step = steps; step >= 0; step--) {
            for (int i = 0; i <= step; i++) {
                stockPriceMaturity[step][i] = initialPrice * upPowers[i] * downPowers[step - i];

                // Initializing the possible options value at expiration
                if (step == steps) {
                    optionValues[step][i] = calculateOptionPayoff(stockPriceMaturity[step][i], strikePrice, isCall);
                } else {
                    optionValues[step][i] = calculateOptionValue(optionValues[step + 1][i], optionValues[step + 1][i + 1], interestRate, q);
                }
            }
        }

        optionPrice = optionValues[0][0];
    }

    /**
     * Computes the risk-neutral probability using discrete compounding.
     */
    private double calculateRiskNeutralProbability(double upFactor, double downFactor, double interestRate) {
        double q = (1 + interestRate - downFactor) / (upFactor - downFactor);
        if (q <= 0 || q >= 1) {
            throw new IllegalArgumentException("Invalid risk-neutral probability; check model parameters");
        }
        return q;
    }

    /**
     * Computes the payoff of the option at expiration.
     */
    private double calculateOptionPayoff(double stockPrice, double strikePrice, boolean isCall) {
        return isCall ? Math.max(stockPrice - strikePrice, 0) : Math.max(strikePrice - stockPrice, 0);
    }

    /**
     * Computes the option value at a given node using discrete discounting.
     */
    private double calculateOptionValue(double payoffUp, double payoffDown, double interestRate, double riskNeutralProb) {
        return (payoffUp * riskNeutralProb + payoffDown * (1 - riskNeutralProb)) / (1 + interestRate);
    }

    private double[] precomputePowers(double factor, int steps) {
        double[] powers = new double[steps + 1];
        powers[0] = 1.0;
        for (int i = 1; i <= steps; i++) {
            powers[i] = powers[i - 1] * factor;
        }
        return powers;
    }

    // Getter methods
    public double getOptionPrice() {
        return optionValues[0][0];
    }

    public double[][] getOptionValues() {
        return optionValues;
    }

    public double[][] getStockPrices() {
        return stockPriceMaturity;
    }
}
