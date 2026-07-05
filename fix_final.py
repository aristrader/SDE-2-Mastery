import os
import shutil

base_dir = "src/main/java/org/example/backend_fundamentals"

# 1. Fix prototype ghosts
proto_dir = os.path.join(base_dir, "design_patterns/creational/playground/prototype")
proto_play = os.path.join(proto_dir, "playground")
os.makedirs(proto_play, exist_ok=True)
for ghost in ["polymorphic", "simple"]:
    src = os.path.join(proto_dir, ghost)
    dst = os.path.join(proto_play, ghost)
    if os.path.exists(src):
        shutil.move(src, dst)
        print(f"Moved {ghost} into playground")

# 2. Pull submodules out of playground
bad_playgrounds = [
    "databases/distributed_transactions/playground",
    "networking/api_design/playground",
    "design_patterns/creational/playground",
    "design_patterns/foundations/playground"
]

for p in bad_playgrounds:
    p_path = os.path.join(base_dir, p)
    if os.path.exists(p_path):
        for item in os.listdir(p_path):
            if item == "index.md": continue
            src = os.path.join(p_path, item)
            dst = os.path.join(os.path.dirname(p_path), item)
            if os.path.exists(dst):
                print(f"Skipping {src} because {dst} exists")
            else:
                shutil.move(src, dst)
                print(f"Pulled {item} out of playground")
        
        leftovers = os.listdir(p_path)
        if not leftovers or leftovers == ['index.md']:
            shutil.rmtree(p_path)
            print(f"Removed empty {p_path}")

