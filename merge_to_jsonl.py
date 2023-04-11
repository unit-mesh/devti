import os
import json
import sys


def json_to_jsonl(source, target, ext='.json'):
    index = 1
    walkdir = os.walk(source)
    with open(target, 'w') as out_file:
        for root, dirs, files in walkdir:
            for file in files:
                if file.endswith(ext):
                    if index == 40000:
                        sys.exit(0)

                    index += 1
                    # format json to one line
                    with open(os.path.join(root, file), 'r') as f:
                        data = json.load(f)
                        json.dump(data, out_file)
                        out_file.write('\n')

json_to_jsonl('datasets/codegen', 'codegen.jsonl')