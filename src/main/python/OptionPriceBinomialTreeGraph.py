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

    # ---------------------------
    # Plot 1: Option Price Evolution
    # ---------------------------
    plt.figure(figsize=(10, 6))
    plt.plot(df['Step'], df['OptionPrice'], marker='o', linestyle='-', color='b', label='Option Price')
    plt.title('Option Price Evolution')
    plt.xlabel('Step')
    plt.ylabel('Option Price')
    plt.grid(True)

    # Annotate overall computation time on the graph
    plt.annotate(f'Computation Time: {computation_time:.4f} seconds',
                 xy=(-0.1, -0.1), xycoords='axes fraction',
                 fontsize=12, color='black',
                 horizontalalignment='left', verticalalignment='top')

    plt.legend()

    # Save the Option Price Evolution plot as an image
    option_plot_path = "src/main/resources/OptionPriceEvolution.png"
    plt.savefig(option_plot_path)
    print(f"Option Price Evolution plot saved successfully at {option_plot_path}.")

    # ---------------------------
    # Plot 2: Computation Time Evolution
    # ---------------------------
    plt.figure(figsize=(10, 6))
    plt.plot(df['Step'], df['ComputationTime'], marker='o', linestyle='-', color='r', label='Computation Time')
    plt.title('Computation Time Evolution')
    plt.xlabel('Step')
    plt.ylabel('Computation Time (milliseconds)')
    plt.grid(True)

    # Annotate overall computation time on the graph
    plt.annotate(f'Total Computation Time: {computation_time:.4f} seconds',
                 xy=(-0.1, -0.1), xycoords='axes fraction',
                 fontsize=12, color='black',
                 horizontalalignment='left', verticalalignment='top')


    plt.legend()

    # Save the Computation Time Evolution plot as an image
    comp_plot_path = "src/main/resources/ComputationTimeEvolution.png"
    plt.savefig(comp_plot_path)
    print(f"Computation Time Evolution plot saved successfully at {comp_plot_path}.")

    # Display both figures in separate windows
    plt.show()

if __name__ == "__main__":
    main()
