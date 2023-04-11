import os
import json


def json_to_jsonl(source, target, ext='.json'):
    walkdir = os.walk(source)
    with open(target, 'w') as out_file:
        for root, dirs, files in walkdir:
            for file in files:
                if file.endswith(ext):
                    # format json to one line
                    with open(os.path.join(root, file), 'r') as f:
                        data = json.load(f)
                        json.dump(data, out_file)
                        out_file.write('\n')
