import os

import pandas as pd
import matplotlib.pyplot as plt

def plot_option_price_evolution(csv_file_path):
    # Read the CSV file using pandas
    try:
        data = pd.read_csv(csv_file_path)
    except FileNotFoundError:
        print(f"File not found: {csv_file_path}")
        return
    except pd.errors.EmptyDataError:
        print("No data found in the CSV file.")
        return
    except pd.errors.ParserError:
        print("Error parsing the CSV file.")
        return

    # Check if required columns exist
    if 'Step' not in data.columns or 'OptionPrice' not in data.columns:
        print("CSV file must contain 'Step' and 'OptionPrice' columns.")
        return

    # Plotting
    plt.figure(figsize=(10, 6))
    plt.plot(data['Step'], data['OptionPrice'], marker='o', linestyle='-', color='b')
    plt.title('Option Price Evolution')
    plt.xlabel('Step')
    plt.ylabel('Option Price')
    plt.grid(True)
    plt.xticks(range(int(data['Step'].min()), int(data['Step'].max()) + 1, max(1, int((data['Step'].max() - data['Step'].min()) / 10))))
    plt.tight_layout()
    plt.show()

if __name__ == "__main__":
    #TODO fix file path
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    csv_file = os.path.join(project_root, "resources", "EvolutionOfOptionPrice.csv")
    plot_option_price_evolution(csv_file)
