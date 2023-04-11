"""
python -m swagger-user-story swagger_to_userstory
python -m swagger-user-story userstory_to_swagger

"""
import openai
import json
from concurrent.futures import ThreadPoolExecutor
import os
import re
import fire

from utils import json_to_jsonl


def encode_prompt(prompt_instructions):
    """Encode multiple prompt instructions into a single string."""
    prompt = open("self-instruct/prompt_cn.txt").read() + "\n"

    for idx, task_dict in enumerate(prompt_instructions):
        (instruction, input, output) = task_dict["instruction"], task_dict["input"], task_dict["output"]
        instruction = re.sub(r"\s+", " ", instruction).strip().rstrip(":")
        input = "<noinput>" if input.lower() == "" else input
        prompt += f"###\n"
        prompt += f"{idx + 1}. Instruction: {instruction}\n"
        prompt += f"{idx + 1}. Input:\n{input}\n"
        prompt += f"{idx + 1}. Output:\n{output}\n"
    prompt += f"###\n"
    prompt += f"{idx + 2}. Instruction:"
    return prompt


def prompt_gpt35(prompt, value):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "system", "content": prompt},
            {"role": "user", "content": f'{value}'},
        ]
    )

    return response.choices[0]["message"]["content"].strip().replace("", "").replace("", "")


def save_item(item, file_name):
    with open(file_name, 'w') as f:
        json.dump(item, f, ensure_ascii=False, indent=4)


def process_swagger(item, i):
    create_user_story_prompt = open("prompts/create_userstory_from_swagger.md").read() + "\n"
    print("processing: ", i)
    output = prompt_gpt35(create_user_story_prompt, item['string'])
    translated_item = {
        "instruction": create_user_story_prompt,
        "input": item['string'],
        "output": output
    }
    save_item(translated_item, f"swagger_output/swagger{i}.json")


def swagger_to_userstory():
    with open('swagger-list.json', 'r') as f:
        data = json.load(f)

    with ThreadPoolExecutor(max_workers=1) as executor:
        futures = {executor.submit(process_swagger, item, i) for i, item in enumerate(data)}


#  merge swagger_output/*.json to one jsonl file
def merge_swagger_output():
    json_to_jsonl('swagger_output', 'swagger_output.jsonl')


def merge_api_output():
    json_to_jsonl('userstory_output', 'userstory_to_api.jsonl')


def userstory_to_swagger():
    data = [json.loads(l) for l in open('swagger_output.jsonl', "r")]

    with ThreadPoolExecutor(max_workers=1) as executor:
        futures = {executor.submit(process_userstory, item, i) for i, item in enumerate(data)}


def process_userstory(item, i):
    # read create_api_prompt.txt.txt as the prompt
    create_api_prompt = open("prompts/create_api_prompt.txt").read() + "\n"

    print("processing user story: ", i)
    # the input will be the output of the previous task
    output = prompt_gpt35(create_api_prompt, item['output'])
    translated_item = {
        "instruction": create_api_prompt,
        "input": item['output'],
        "output": output
    }
    save_item(translated_item, f"userstory_output/userstory{i}.json")


def main(task, **kwargs):
    if not os.path.exists('userstory_output'):
        os.makedirs('userstory_output')
    if not os.path.exists('swagger_output'):
        os.makedirs('swagger_output')
    globals()[task](**kwargs)


if __name__ == "__main__":
    fire.Fire(main)
