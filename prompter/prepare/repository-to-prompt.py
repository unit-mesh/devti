"""
python -m repository-to-prompt process_prompt
"""
import os

import openai
import json
from concurrent.futures import ThreadPoolExecutor
import fire


def prompt_gpt35(prompt, value):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "system", "content": prompt},
            {"role": "user", "content": f'{value}'},
        ]
    )

    return response.choices[0]["message"]["content"].strip().replace("", "").replace("", "")


def process_prompt():
    # open repositories.json
    with open("repositories.json", "r") as f:
        data = json.load(f)

    with ThreadPoolExecutor(max_workers=1) as executor:
        futures = {executor.submit(process_repository, item, i) for i, item in enumerate(data)}


prompt_text = "请编写用户故事，能覆盖下面的代码功能，要求：1. 描述业务并突出重点 2. 你返回的内容只有： 我想 xxx，以便于。 3. " \
              "带上表和字段信息，示例：`我想查找用户（user）在某个时间段（receiptDate）内的物品清单（items），以便于进行统计和分析。`"


def process_repository(item, i):
    output_file = f"repositories/repository-{item['id']}.json"
    if os.path.exists(output_file):
        print(f"skipping {item['id']}")
        return

    print("processing user story: ", i)
    # the input will be the output of the previous task
    output = prompt_gpt35(prompt_text, item['content'])
    translated_item = {
        "instruction": prompt_text,
        "input": item['content'],
        "output": output
    }
    save_item(translated_item, output_file)


def save_item(item, file_name):
    with open(file_name, 'w') as f:
        json.dump(item, f, ensure_ascii=False, indent=4)


def main(task, **kwargs):
    globals()[task](**kwargs)


if __name__ == "__main__":
    fire.Fire(main)
