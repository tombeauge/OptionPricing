import java.util.Arrays;

public class MultiStepBinomialTree {
    private final double optionPrice;
    private final double[][] optionValues;
    private final double[][] stockPrices;
    double[][] stockPriceMaturity;

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
        stockPriceMaturity = new double[steps + 1][steps + 1];

        //TO DO: optimise array size
        optionValues = new double[steps + 1][steps + 1];
        stockPrices = new double[steps + 1][steps + 1];

        // Backward induction
        for (int step = steps; step >= 0; step--) {

            for (int i = 0; i <= step; i++) {

                int ups = i;
                int downs = step - i;
                stockPriceMaturity[step][i] = initialPrice * Math.pow(upFactor, ups) * Math.pow(downFactor, downs);

                // Initialising the possible options value at expiration
                optionValues[step][i] = calculateOptionPayoff(stockPriceMaturity[step][i], strikePrice, isCall);

                if (step == steps){
                    optionValues[step][i] = calculateOptionPayoff(stockPriceMaturity[step][i], strikePrice, isCall);
                }
                else {
                    optionValues[step][i] = calculateOptionValue(optionValues[step + 1][i], optionValues[step + 1][i + 1], interestRate, q);
                }

                //stockPrices[step][i] = calculateOptionPayoff(stockPriceMaturity[step][i], strikePrice, isCall);

            }
        }

        optionPrice = optionValues[0][0];

        System.out.println("Option price: " + optionPrice);
    }

    private double calculateRiskNeutralProbability(double upFactor, double downFactor, double interestRate) {
        double q = (Math.exp(interestRate) - downFactor) / (upFactor - downFactor);
        if (q < 0 || q > 1) {
            throw new IllegalArgumentException("Invalid risk-neutral probability; check model parameters");
        }
        return q;
    }

    private double calculateOptionPayoff(double stockPrice, double strikePrice, boolean isCall) {
        return isCall ? Math.max(stockPrice - strikePrice, 0) : Math.max(strikePrice - stockPrice, 0);
    }

    private double calculateOptionValue(double payoffUp, double payoffDown, double interestRate, double riskNeutralProb) {
        return (payoffUp * riskNeutralProb + payoffDown * (1 - riskNeutralProb)) * Math.exp(interestRate);
    }

    // Getter methods
    public double getOptionPrice() {
        return optionValues[0][0];
    }

    public double[][] getOptionValues() {
        return optionValues;
    }

    public double[][] getStockPrices() {
        System.out.printf("Stock Prices: " + Arrays.deepToString(stockPriceMaturity));
        return stockPriceMaturity;
    }


}
