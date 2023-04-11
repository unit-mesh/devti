"""
python -m user-story create_user_story_map
"""
import concurrent
import csv
import json
import os
import re
import time
from concurrent.futures import ThreadPoolExecutor

import fire
import openai
import tqdm

import utils


def init_domains():
    domains = []
    with open("data/website.csv") as fd:
        rd = csv.reader(fd, delimiter=",", quotechar='"')
        for row in rd:
            if len(row) > 1:
                domains.append(row[1])

    return domains


output_dir = 'userstory_map'


def merge_created_user_story():
    utils.json_to_jsonl("userstory_map", "userstory_map.jsonl")


# output: [{ "input": domain, "output": list of user stories }]
def prepare_story_list():
    with open('userstory_map.jsonl', 'r') as f:
        data = [json.loads(l) for l in f]

    output = []
    for item in data:
        origin_stories = item['output']
        pattern = r'\d+\.\s+(.*)'
        matches = re.findall(pattern, origin_stories)

        for match in matches:
            output.append({
                "input": item['input'],
                "output": match
            })

    with open('userstory_name.jsonl', 'w') as f:
        for item in output:
            f.write(json.dumps(item) + '\n')


def create_user_story_detail():
    data = [json.loads(l) for l in open('userstory_name.jsonl', "r")]

    with open("prompts/create-user-story.md", 'r') as file:
        base_prompt = file.read()

    idx = 1

    total = len(data)
    progress_bar = tqdm.tqdm(total=total)

    # mkdir userstory_detail
    os.makedirs("userstory_detail", exist_ok=True)

    for item in data:
        if os.path.exists(f"userstory_detail/{idx}.json"):
            idx = idx + 1
            progress_bar.update()
            continue

        prompt = base_prompt.replace("${domain}", item['input']).replace("${story_name}", item['output'])

        try:
            res = prompt_davinci(prompt)
            progress_bar.update()

            output = {
                "input": item['input'] + ":" + item['output'],
                "output": res
            }

            # write to file in test_to_code
            with open(f"userstory_detail/{idx}.json", 'w') as file:
                json.dump(output, file)

            # sleep_time = 3
            idx = idx + 1
        except Exception as e:
            print(e)
            print("Error, sleeping for 5 minutes")
            time.sleep(300)
            continue


def create_user_tasks():
    domains = init_domains()

    os.makedirs(output_dir, exist_ok=True)

    total = len(domains)
    progress_bar = tqdm.tqdm(total=total)

    with open("prompts/create-user-story-name.md", 'r') as file:
        base_prompt = file.read()

    process_story(base_prompt, domains, progress_bar)

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = {executor.submit(process_story, base_prompt, item, i) for i, item in enumerate(domains)}
        for future in concurrent.futures.as_completed(futures):
            progress_bar.update()


def process_story(base_prompt, domain, idx):
    if os.path.exists(f"{output_dir}/{idx}.json"):
        return

    prompt = base_prompt.replace("${domain}", domain)

    try:
        res = prompt_davinci(prompt)

        output = {
            "input": domain,
            "output": res
        }

        # write to file in test_to_code
        with open(f"{output_dir}/{idx}.json", 'w') as file:
            json.dump(output, file)

    except Exception as e:
        print(e)
        print("Error, sleeping for 5 minutes")
        time.sleep(30)


def merge_userstory_details():
    source = "userstory_detail"
    target = "userstory_detail.jsonl"

    walkdir = os.walk(source)
    with open(target, 'w') as out_file:
        for root, dirs, files in walkdir:
            for file in files:
                if file.endswith(".json"):
                    # format json to one line
                    with open(os.path.join(root, file), 'r') as f:
                        data = json.load(f)
                        data["instruction"] = "create Agile user story for following topic"
                        json.dump(data, out_file)
                        out_file.write('\n')


# add instruction field to userstory_map.jsonl
def add_instruction():
    with open("userstory_map.jsonl", 'r') as f:
        data = [json.loads(l) for l in f]

    for item in data:
        item["instruction"] = "split user story into tasks"

    with open("userstory_map.jsonl", 'w') as f:
        for item in data:
            f.write(json.dumps(item) + '\n')


# merge userstory_map.jsonl and userstory_detail.jsonl
def merge_userstory_map_and_detail():
    with open("userstory_map.jsonl", 'r') as f:
        data = [json.loads(l) for l in f]

    with open("userstory_detail.jsonl", 'r') as f:
        data.extend([json.loads(l) for l in f])

    with open("userstory_map_and_detail.jsonl", 'w') as f:
        for item in data:
            f.write(json.dumps(item) + '\n')


def prompt_davinci(prompt):
    response = openai.Completion.create(
        model="text-davinci-003",
        prompt=prompt,
        temperature=0,
        max_tokens=3096,
        top_p=1.0,
        frequency_penalty=0.0,
        presence_penalty=0.0,
        stop=["\"\"\""]
    )
    res = response['choices'][0]['text']
    return res


def user_story_format():
    # load all files under userstory_map/{*.json}
    # parse each file
    with open("userstory_map/1.json") as file:
        parse_user_story(json.load(file))


def parse_user_story(item):
    output_str = item['output']
    result = parse_string(output_str)
    print(result)


def parse_string(s):
    # 使用正则表达式来匹配大括号内的内容
    pattern = r"\{\s*([^{}]+)\s*\}"
    matches = re.findall(pattern, s)

    result = {}

    for match in matches:
        sections = re.split(r"\s*,\s*", match.strip())

        sub_dict = {}

        for section in sections:
            sub_dict[section] = None

        parent_match = re.search(r"^\s*(\S+)\s*", match)

        if parent_match:
            parent = parent_match.group(1)
            result[parent] = sub_dict

    return result


def main(task, **kwargs):
    globals()[task](**kwargs)


if __name__ == "__main__":
    fire.Fire(main)
