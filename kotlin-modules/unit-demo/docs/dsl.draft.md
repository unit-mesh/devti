# Unit Mesh DSL

## API 要素 ？

### URI

一个 API 请求

```
GET /users/{id}
request:
{
  "id": 1
}
response:
{
  "id": 1,
  "name": "John",
  "age": 20
}
```

### Endpoint-Action

Endpoint

```java

@RequestMapping("/users")
class UserController {
    constructor(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/{id}")
    public User getUser(@PathVariable("id") int id) {
        return userService.getUser(id);
    }
}
```

Action

```java
class UserService {
    public User getUser(int id) {
        return new User(id, "John", 20);
    }
}
```

### 那么

```java
// Controller/Endpoint 
// Service/Logic
// Repository/DataSchema
```

- Controller 模板
- AI 将逻辑转换为代码
- Repository 模板 + SQL 查询

## Output

[https://github.com/square/kotlinpoet](https://github.com/square/kotlinpoet)

## Step 1. Text to Repository

1. SQL to Text
2. SQL Prompter
3. Text to Prompt Template (with DB), Prompt to SQL

Format:

```java
@Service
public class UserService(Database database) {
    public User getUser(int id) {
        return database.queryForObject("SELECT * FROM users WHERE id = ?", User.class, id);
    }
}
```

如果都是模板代码，就可以直接生成代码了 + Usecases

```java
@Service
public class CreateUserUsecase(Database database) {
    public User createUser(User user) {
        return database.queryForObject("INSERT INTO users (name, age) VALUES (?, ?)", User.class, user.name, user.age);
    }
} 
```

或者 CreateBlogComment

```java
@Service
public class CreateBlogCommentUsecase(Database database) {
    public BlogComment createBlogComment(BlogComment blogComment) {
        return database.queryForObject("INSERT INTO blog_comments (blog_id, user_id, content) VALUES (?, ?, ?)", BlogComment.class, blogComment.blogId, blogComment.userId, blogComment.content);
    }
} 
```

## Step 2. Text to API


