# Unit PreProcessor 

> Unit Processor 是一个代码分析和处理引擎，用于在 AI 编程的前半部分的代码分析和处理。

主要用于对 Unit Mesh 所需要的代码结果进行预处理，比如对 Unit 进行语法分析，生成对应的格式。

1. 结合 Unit Crawler （由于法律原因，开源规划中）从 GitHub 上爬取模块
2. 对源码进行语法分析，分解出不同的 API Unit
   - Swagger (API Design)
   - Controller, Service, Model, Repository, etc.
   - User Story
   - Test
   - Requirements Diff
3. 根据 Unit Prompter（暂未开源）的 prompt 生成对应的格式

## Todos

- [ ] Change AST to Chapi to support multiple languages
- [ ] Add more unit types
- [ ] Add more unit parsers

## LICENSE

This code is distributed under the MPL 2.0 license. See `LICENSE` in this directory.
