import json
import os

with open('ac4d34cff736dfc79344fe1ed9b40b54f9b123c9e381a826a34045a85bde2c6c-2026-06-20-23-01-01-addcb7fb0c69414ab36d928d3618f0ff/conversations.json') as f:
    data = json.load(f)

# Sort by create time descending
data.sort(key=lambda x: x.get('create_time', 0), reverse=True)

cutoff_idx = -1
for i, c in enumerate(data):
    title = c.get('title')
    if title and 'retirement corpus calculation' in title.lower():
        cutoff_idx = i
        break

if cutoff_idx == -1:
    print("Cutoff not found!")
    exit(1)

chats = data[:cutoff_idx]
exclude = ["Google Survey Rewards Issue"]

os.makedirs('raw_chats', exist_ok=True)
count = 0
for i, chat in enumerate(chats):
    title = chat.get('title', 'No Title')
    if title in exclude:
        continue
    
    current_node = chat.get('current_node')
    mapping = chat.get('mapping', {})
    
    if not current_node:
        leaves = [k for k, v in mapping.items() if not v.get('children')]
        current_node = leaves[0] if leaves else None
        
    path = []
    while current_node:
        node = mapping.get(current_node)
        if not node:
            break
        path.append(node)
        current_node = node.get('parent')
        
    path.reverse()
    
    transcript = f"# {title}\n\n"
    for node in path:
        msg = node.get('message')
        if msg:
            author = msg.get('author', {}).get('role', 'unknown')
            if author in ['user', 'assistant']:
                content = msg.get('content', {}).get('parts', [])
                if content:
                    text = "".join([str(p) for p in content if isinstance(p, str)])
                    if text.strip():
                        transcript += f"### {author.upper()}\n{text}\n\n"
                        
    safe_title = "".join([c if c.isalnum() else "_" for c in title])
    filename = f"raw_chats/{count:02d}_{safe_title}.md"
    with open(filename, 'w') as f:
        f.write(transcript)
    count += 1
print(f"Extracted {count} chats to raw_chats/")
