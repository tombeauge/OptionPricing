import subprocess
import os
import glob

# Define the Java file and class name
project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
java_dir= os.path.join(project_root, "java")


# Change working directory to java_dir
os.chdir(java_dir)

# Optionally, clean previous .class files
for class_file in glob.glob('*.class'):
    os.remove(class_file)

# Find all Java files
java_files = glob.glob('*.java')

if not java_files:
    print(f"No Java files found in directory {java_dir}.")
    exit(1)

# Step 1: Compile all Java files
compile_command = ['javac'] + java_files
print(f"Compiling Java files: {' '.join(java_files)}...")
compile_process = subprocess.run(compile_command, capture_output=True, text=True)

if compile_process.returncode != 0:
    print("Compilation failed with the following error:")
    print(compile_process.stderr)
    exit(1)
else:
    print("Compilation successful.")

# Step 2: Run the main Java class
class_name = 'OptionPricerGUI'
run_command = ['java', class_name]
print(f"Running {class_name}...")
run_process = subprocess.run(run_command, capture_output=True, text=True)

if run_process.returncode != 0:
    print("Execution failed with the following error:")
    print(run_process.stderr)
    exit(1)
else:
    print("Execution successful. Output:")
    print(run_process.stdout)