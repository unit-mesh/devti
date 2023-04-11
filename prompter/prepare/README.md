# Prepare

- [user-story.py](user-story.py)，调用 OpenAI 的用户故事拆解和用户故事生成。
- [test-to-code.py](test-to-code.py) 测试代码与处理。
- [swagger-user-story.py](swagger-user-story.py) Swagger 和用户故事相互转换。
- [repository-to-prompt.py](repository-to-prompt.py)，调用 OpenAI 的代码生成，从 Repository 代码生成 Prompt。

## 数据生成

配置 OpenAI 的 API Key

```bash
export OPENAI_API_KEY=sk-xxxx
```

### 用户故事数据生成

生成用户故事任务

```bash
# 生成用户故事任务
python user-story.py create_user_tasks

# 合并为 JSONL
python user-story.py merge_created_user_story

# 添加 instruction
python user-story.py add_instruction

# 创建用户故事
python user-story.py create_user_story_detail

# 合并为 JSONL
python user-story.py merge_userstory_details
```

### Swagger 数据生成

```bash
# 根据 Swagger 生成用户故事
python swagger-user-story.py swagger_to_userstory

# 合并为 JSONL
python swagger-user-story.py merge_swagger_output

# 根据用户故事生成 Swagger
python swagger-user-story.py userstory_to_swagger

# 合并为 JSONL
python swagger-user-story.py merge_api_output
```

### 测试代码数据生成

```bash
# 合并 Processor 输出为 JSONL
python test-to-code.py merge_test_to_jsonl

# 从测试生成代码
python test-to-code.py generate_code_from_tests

# 合并为 JSONL
python test-to-code.py merge_test_output_to_jsonl

# 生成微调所需要的 instruction
python test-to-code.py generate_for_lora
```

### Repository 代码数据生成

```bash
# 生成 Repository 代码
python repository-to-prompt.py process_prompt
```

### 生成辅助代码

不需要
