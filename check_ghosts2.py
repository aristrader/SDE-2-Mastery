import os

base_dir = "src/main/java/org/example/backend_fundamentals"
excluded = ['playground', 'exercise', 'solution', 'assets', 'design', 'todo', '.git']
bad_playgrounds = [
    "databases/distributed_transactions/playground",
    "networking/api_design/playground",
    "design_patterns/creational/playground",
    "design_patterns/foundations/playground"
]

for p in bad_playgrounds:
    p_path = os.path.join(base_dir, p)
    for module in os.listdir(p_path):
        mod_path = os.path.join(p_path, module)
        if os.path.isdir(mod_path):
            for sub in os.listdir(mod_path):
                if sub not in excluded and os.path.isdir(os.path.join(mod_path, sub)):
                    print(f"LOOSE PACKAGE: {os.path.join(mod_path, sub)}")

