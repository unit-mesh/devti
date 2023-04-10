# Prompt Template

## 领域词汇表

| 中文      | 英文                       | 说明 |
|---------|--------------------------|----|
| 业务      | Business                 |    |
| 所有xxx   | All xxx                  |    |
| 时间内     | In time                  |    |
| 特定时间范围内 | In a specific time range |    |
| 查询      | Query                    |    |
| 某个用户    | A specific user          |    |

## 表信息

| 表名   | 字段            | 说明 |
|------|---------------|----|
| user | id, name, age |    |


作为一个用户，我想查找某个用户拥有的在一个特定时间范围内收到的所有项目，以便了解他们在这段时间内的支出情况。

- 为此，我需要将该用户的用户名作为参数传递给查询，以及指定查询的时间范围。然后，
- 系统将从项目表中选择所有符合条件的项目，并以列表的形式返回给我
- 如果没有符合条件的项目，系统将返回一个空列表。


## Prompt

原始 SQL:

```sqlite
select i from Item i where i.user = :user and :before <= i.receiptDate and i.receiptDate <= :after
```

```markdown
> 围绕下面的用户故事，编写一段 SQL 代码： 

###

作为一个用户，我想查找某个用户拥有的在一个特定时间范围内收到的所有项目，以便了解他们在这段时间内的支出情况。

为此，我需要将该用户的用户名作为参数传递给查询，以及指定查询的时间范围。然后，
系统将从项目表中选择所有符合条件的项目，并以列表的形式返回给我
如果没有符合条件的项目，系统将返回一个空列表。
###
```

返回

```
你好，我可以为你提供一段 SQL 代码来查找某个用户拥有的在一个特定时间范围内收到的所有项目，以便了解他们在这段时间内的支出情况：

SELECT *
FROM items
WHERE user_name = [username]
AND date_received BETWEEN [start_date] AND [end_date];

如果没有符合条件的项目，这段代码将返回一个空列表。
```

更新模板

```markdown
> 围绕下面的用户故事，编写一段 SQL 代码： 

###

作为一个用户，我想查找某个用户（User）拥有的在一个特定时间范围内（receiptDate）收到的所有项目（Item），以便了解他们在这段时间内的支出情况。

为此，我需要将该用户的用户名（username）作为参数传递给查询，以及指定查询的时间范围。然后，
系统将从项目表中选择所有符合条件的项目，并以列表的形式返回给我
如果没有符合条件的项目，系统将返回一个空列表。
###
```

结果：

```
你好，我可以为你提供一段 SQL 代码来查找某个用户拥有的在一个特定时间范围内收到的所有项目，以便了解他们在这段时间内的支出情况：

SELECT * 
FROM items 
WHERE username = [username] 
AND receiptDate BETWEEN [start_date] AND [end_date];

如果没有符合条件的项目，这段代码将返回一个空列表。
```


```
请编写用户故事，能覆盖下面的代码功能，要求：1. 突出重点 2. 你返回的内容只有： 我想 xxx。

###
interface ZhuanlanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(zhuanlanBean: ZhuanlanBean): Long
}
###
```
