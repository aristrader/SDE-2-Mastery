import os
import re

base_dir = "src/main/java/org/example/backend_fundamentals"

for root, dirs, files in os.walk(base_dir):
    for f in files:
        if f.endswith('.java'):
            filepath = os.path.join(root, f)
            rel_path = os.path.relpath(root, "src/main/java")
            correct_pkg = rel_path.replace(os.sep, '.')
            
            with open(filepath, 'r') as file:
                content = file.read()
                
            new_content = re.sub(r'^package\s+[\w\.]+;', f'package {correct_pkg};', content, flags=re.MULTILINE)
            
            if new_content != content:
                print(f"Fixed package in {filepath}")
                with open(filepath, 'w') as file:
                    file.write(new_content)
