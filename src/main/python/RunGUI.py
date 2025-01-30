import sys
import pandas as pd
import matplotlib.pyplot as plt

def main():
    if len(sys.argv) < 2:
        print("No computation time provided.")
        sys.exit(1)

    try:
        computation_time = float(sys.argv[1])
    except ValueError:
        print("Invalid computation time provided.")
        sys.exit(1)

    # Define the path to the CSV file
    csv_path = "src/main/resources/EvolutionOfOptionPrice.csv"

    # Read the CSV file
    try:
        df = pd.read_csv(csv_path)
    except FileNotFoundError:
        print(f"CSV file not found at {csv_path}.")
        sys.exit(1)

    # Plot the Option Price vs. Steps
    plt.figure(figsize=(10, 6))
    plt.plot(df['Step'], df['OptionPrice'], marker='o', linestyle='-', color='b', label='Option Price')
    plt.title('Option Price Evolution')
    plt.xlabel('Step')
    plt.ylabel('Option Price')
    plt.grid(True)

    # Annotate computation time on the graph
    plt.annotate(f'Computation Time: {computation_time:.4f} seconds',
                 xy=(0.05, 0.95), xycoords='axes fraction',
                 fontsize=12, color='red',
                 horizontalalignment='left', verticalalignment='top',
                 bbox=dict(boxstyle='round,pad=0.5', facecolor='yellow', alpha=0.5))

    # Add legend
    plt.legend()

    # Save the plot as an image
    plot_path = f"src/main/resources/OptionPriceEvolution_{df['Step'].max()}.png"
    plt.savefig(plot_path)
    plt.close()

    print(f"Plot saved successfully at {plot_path}.")

if __name__ == "__main__":
    main()
