# Importer

- Apache Arrow as Format for speed
- Hashmap as Index for speed
- JSON as Format for compatibility

## Prompt 

### 默认情况

```
请编写用户故事，能覆盖下面的代码功能，要求：1. 突出重点 2. 你返回的内容只有： 我想 xxx。

@Insert(onConflict = OnConflictStrategy.IGNORE)
fun insert(zhuanlanBean: ZhuanlanBean): Long
```

输出结果：

```
一个用户想要在数据库中插入一个ZhuanlanBean，但他们希望如果这个ZhuanlanBean已经存在，则忽略这次插入操作。
```

### 理想情况

```
请编写用户故事，能覆盖下面的代码功能，要求：1. 突出重点 2. 你返回的内容只有： 我想 xxx，以便于。
领域词汇：专栏（ZhuanlanBean）

@Insert(onConflict = OnConflictStrategy.IGNORE)
fun insert(zhuanlanBean: ZhuanlanBean): Long
```

结果：

```
一个用户想要将一篇专栏文章存入数据库，但他们希望如果这篇文章已经存在，则忽略这次存储操作，而不是覆盖原有的文章。
```
