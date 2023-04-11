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

