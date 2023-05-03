package cc.unitmesh.core.java

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShortClassTest {
    @Test
    fun `test short class to string`() {
        val shortClass = ShortClass(
            className = "TestClass",
            packageName = "org.unitmesh.processor",
            fields = listOf(
                ShortField(
                    fieldName = "field1",
                    dataType = "String",
                ),
                ShortField(
                    fieldName = "field2",
                    dataType = "Int",
                ),
            ),
            methods = listOf(
                ShortMethod(
                    methodName = "method1",
                    returnType = "String",
                    parameters = listOf(
                        ShortParameter(
                            parameterName = "param1",
                            dataType = "String",
                        ),
                        ShortParameter(
                            parameterName = "param2",
                            dataType = "Int",
                        ),
                    ),
                ),
                ShortMethod(
                    methodName = "method2",
                    returnType = "Int",
                ),
            ),
            constructors = listOf(
                ShortParameter(
                    parameterName = "param1",
                    dataType = "String",
                ),
                ShortParameter(
                    parameterName = "param2",
                    dataType = "Int",
                ),
            ),
        )
        val expected = """
org.unitmesh.processor.TestClass(String, Int)
- fields: field1:String, field2:Int
- methods: method1(String, Int): String, method2(): Int
        """.trimIndent()
        assertEquals(expected, shortClass.toString())
    }

    @Test
    fun `test short class to string without fields`() {
        val sourceCode = """public class PostService extends FormatDate {

    private PostRepository postRepository;
    private UserRepository userRepository;
    private Set<Post> userPosts = new HashSet<>();
    private ImageService imageService;

    public PostService(PostRepository postRepository,UserRepository userRepository, ImageService imageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post addNewPost(Post post) {
        //post.setCreatedBy("ADMIN");
        Optional<User> userEmail =  userRepository.findByEmail("admin@gmail.com"); // CHANGE IT IN PRODUCTION
        postRepository.save(post);
        userPosts.add(post);
        post.setUser(userEmail.get());
        userRepository.save(userEmail.get());
        return post;
    }

    public int saveImageToPost(String name, MultipartFile file, Post post) {
        Post newPost = addNewPost(post);
        try {
            byte[] image = file.getBytes();
            Image model = new Image(name, image);
            int saveImage = imageService.saveImage(model);

            if (saveImage == 1)
            newPost.setImage(model);

             newPost.setMyDate(formatDateToDayMonthYear());

            byte[] encodingImage = Base64.getEncoder().encode(image);
            String saveEncodedString = new String(encodingImage, "UTF-8");
            newPost.getImage().setImageString(saveEncodedString);
            postRepository.save(newPost);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
}

"""
        val toShortClass = JavaProcessor(sourceCode).toShortClass()
        val expected = """
    PostService(PostRepository, UserRepository, ImageService)
    - fields: postRepository:PostRepository, userRepository:UserRepository, userPosts:Set<Post>, imageService:ImageService
    - methods: findAll(): List<Post>, addNewPost(Post): Post, saveImageToPost(String, MultipartFile, Post): int
        """.trimIndent()
        assertEquals(expected, toShortClass.toString())
    }

    @Test
    fun `test short class to string without fields and methods`() {
        val sourceCode = """@Repository
@Transactional
public interface PostRepository extends JpaRepository<Post, Long> {


    Optional<Post> findById(Long id);

    @Modifying
    @Query("update Post p set p.postCommentsSize = :commentsSize where p.id = :id")
    void updatePostCommentsSize(@Param("commentsSize") int postCommentsSize, @Param("id") Long id);


    // find all post with releated topic

    List<Post> findAllByPostTopics(String postTopic);

    // and sort them date
    List<Post> findDistinctByPostTopics(String postTopics);


}"""
        val toShortClass = JavaProcessor(sourceCode).toShortClass()
        val expected = """PostRepository()
- methods: findById(Long): Optional<Post>, updatePostCommentsSize(int, Long): void, findAllByPostTopics(String): List<Post>, findDistinctByPostTopics(String): List<Post>"""
        assertEquals(expected, toShortClass.toString())
    }
}
