# Processor

> Processor 是一个代码分析和处理引擎，用于在 AI 编程的前半部分的代码分析和处理。

对源码进行语法分析，分解出不同的 Unit

- Common
    - Cli-Core. core cli
    - Prompter. LLM prompter
    - Core. core models
    - Verifier. UML, SQL, Markdown, etc.
- Java
    - Codegen Processor.
    - Core Analysis. Java Syntax Analysis
    - Spring Processor. Controller, Service, Model, Repository, etc.
    - Test Processor.
- Kotlin
    - Syntax Analysis
    - Importer（Text to Repository）
- Modules
    - API Processor
    - PlantUML Verifier
    - SQL Verifier

## LICENSE

API Processor module postman based on [https://github.com/poynt/postman-runner](https://github.com/poynt/postman-runner)
with Apache 2.0 license.

This code is distributed under the MPL 2.0 license. See `LICENSE` in this directory.
