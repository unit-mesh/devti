# DevTi - 开发钛

> DevTi（Development + Titanium）一款基于大语言模型的研发效能提升的开源项目。旨在基于 LLM 的微调来提供全面智能化解决方案，助力开发人员高效完成开发任务，以实现自动化用户任务拆解、用户故事生成、自动化代码生成、自动化测试生成等等。

理想中的 DevTi 简介（ChatGPT 生成）：

DevTi是一款基于人工智能的开源项目，旨在为开发人员提供一套全面的、智能化的解决方案，从而帮助他们更加高效地完成各种开发任务。

DevTi 的核心功能包括自动化代码生成、智能代码检查、自动化测试和部署等，这些功能都依赖于AI技术的强大支持。例如，DevTi 可以通过分析代码库中的模式和规律，自动生成符合规范的代码，从而减少了繁琐的手动编写工作。此外，DevTi 还可以实时检查代码质量，发现潜在的错误和安全漏洞，并提供相应的建议，从而减少了后续的修复和调试工作。

除此之外，DevTi 还提供了丰富的数据分析和可视化功能，帮助开发人员更好地了解和分析代码库的性能和质量状况。同时，DevTi 还支持多种编程语言和开发框架，可以适应不同的开发需求和场景。

## About DevTi

![DevTi](https://unitmesh.cc/images/devti-processes.png)

特性:

- 全流程研发效能提升
- 端到端的 AI 辅助生成
	- 需求拆解
	- 分析用户故事
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

模块：

- Collector（Python, JavaScript），数据收集
- Processor（Kotlin），数据处理
- Prompter（Python），Prompt 设计、调整、优化等
- Train（Python），训练相关的 Notebook
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
