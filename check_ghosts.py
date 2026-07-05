import os

base_dir = "src/main/java/org/example/backend_fundamentals"
excluded = ['playground', 'exercise', 'solution', 'assets', 'design', 'todo', '.git']

for root, dirs, files in os.walk(base_dir):
    dirs[:] = [d for d in dirs if d not in excluded]
    for d in dirs:
        d_path = os.path.join(root, d)
        if not os.path.exists(os.path.join(d_path, "index.md")):
            print(f"GHOST FOLDER: {d_path}")

