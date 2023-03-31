# PreProcessor 

用途：

1. 对源码的 Controller 进行语法分析，分解出不同的 API Unit
2. 使用 AI 对不同的 API Unit 进行自然语言分析，生成语义
3. 结合 API 生成数据，类似于 `alpaca-lora`

最终 Output 数据：

```yaml
- instruction:
  input:
  output:
```

## PreProcessor

1. read config with repositories
2. clone all repositories depth = 1
3. filter all tests file
   - remove license for reduce prompt size 
   - split test cases to different output
4. parse all to data

