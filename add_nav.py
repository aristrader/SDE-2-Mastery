import os

base_dir = "src/main/java/org/example/backend_fundamentals"

for root, dirs, files in os.walk(base_dir):
    if 'exercise' in dirs:
        index_file = os.path.join(root, 'index.md')
        if os.path.exists(index_file):
            with open(index_file, 'r') as f:
                content = f.read()
            if '<ExerciseNav' not in content:
                print(f"Adding ExerciseNav to {index_file}")
                with open(index_file, 'a') as f:
                    f.write('\n\n<ExerciseNav />\n')

