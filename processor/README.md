# Unit Processor 

> Unit Processor 是一个代码分析和处理引擎，用于在 AI 编程的前半部分的代码分析和处理。

主要用于对 [github-crawler](https://github.com/unit-mesh/github-crawler) 所需要的代码结果进行预处理，比如对 Unit 进行语法分析，生成对应的格式。

1. 结合 Unit Crawler 从 GitHub 上爬取模块
2. 对源码进行语法分析，分解出不同的 API Unit
   - Swagger (API Design)
   - Controller, Service, Model, Repository, etc.
   - User Story
   - Test
   - Requirements Diff
3. 根据 [Unit Minions Data Prepare](https://github.com/unit-mesh/minions-data-prepare) 的 prompt 生成对应的格式

## LICENSE

This code is distributed under the MPL 2.0 license. See `LICENSE` in this directory.
