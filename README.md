# DevTi - 开发钛

[![CI](https://github.com/unit-mesh/devti/actions/workflows/ci.yml/badge.svg)](https://github.com/unit-mesh/devti/actions/workflows/ci.yml)

> DevTi（Development + Titanium）一款基于大语言模型的研发效能提升的开源项目。旨在基于 LLM 的微调来提供全面智能化解决方案，助力开发人员高效完成开发任务，以实现自动化用户任务拆解、用户故事生成、自动化代码生成、自动化测试生成等等。

## About DevTi

![DevTi](https://unitmesh.cc/images/devti-processes.png)

特性:

- 全流程研发效能提升
- 端到端的 AI 辅助生成
	- 需求拆解
	- 拆分、分解用户故事
	- 测试代码生成
	- 代码辅助生成
	- ……
- 研发实践辅助导入
- 研发规范自检内嵌

## Usage

Instruction:

- 拆分任务。instruction：split user story tasks，input：折分用户故事任务
- 需求细化。instruction：create Agile user story for following topic，input：功能的基本信息
- 代码生成。instruction：Implement the method xxx，input：类的基本信息
- 测试生成。instruction：Write test for follow code，input：类的基本信息
- Repository 生成。instruction：text to repository with class，input：功能的基本信息

## Development

![DevTi](https://unitmesh.cc/images/devti.png)

文档详细见各模块的 README.md

模块：

- [Collector](./collector)（Python, JavaScript），数据收集
- [Processor](./processor)（Kotlin），数据处理
- [Prompter](./prompter)（Python），Prompt 设计、调整、优化等
- [Train](./train)（Python），训练相关的 Notebook
- Chain（Python），开发框架

Todos：

- [x] 端到端的 AI 辅助生成
	- [x] 需求拆解
	- [x] 分析用户故事
	- [x] 测试代码生成
	- [x] 代码辅助生成
	- SQL 生成
	- ...
- [ ] 研发实践辅助导入
- [ ] 研发规范自检内嵌

更新中...

## LICENSE

This code is distributed under the MPL 2.0 license. See `LICENSE` in this directory.
