package ru.yandex.practicum.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.blog.config.WebMvcConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebMvcConfig.class)
@TestPropertySource(properties = {
        // Integration-test pattern: override infra settings in test context only.
        // This keeps tests deterministic and independent of local PostgreSQL setup.
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:blog_mvc_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class PostControllerMvcTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getApiPostsShouldReturnPagedFeedJson() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "anything")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "posts": [
                            {
                              "id": 1,
                              "likesCount": 5,
                              "commentsCount": 1
                            },
                            {
                              "id": 2,
                              "likesCount": 2,
                              "commentsCount": 2
                            }
                          ],
                          "hasPrev": false,
                          "hasNext": false,
                          "lastPage": 1
                        }
                        """, false));
    }

    @Test
    void postApiPostsIdShouldReturnPostJson() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "likesCount": 5,
                          "commentsCount": 1
                        }
                        """, false));
    }

    @Test
    void postApiPostsIdShouldReturn404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void postApiPostsShouldCreatePostAndReturnJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Post from test",
                                  "text": "Markdown text",
                                  "tags": ["java", "spring"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "id": 3,
                          "title": "Post from test",
                          "text": "Markdown text",
                          "tags": ["java", "spring"],
                          "likesCount": 0,
                          "commentsCount": 0
                        }
                        """, true));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void putApiPostsIdShouldUpdatePostAndReturnJson() throws Exception {
        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "title": "Updated title",
                                  "text": "Updated markdown text",
                                  "tags": ["backend", "java"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "title": "Updated title",
                          "text": "Updated markdown text",
                          "tags": ["backend", "java"],
                          "likesCount": 5,
                          "commentsCount": 1
                        }
                        """, true));
    }

    @Test
    void putApiPostsIdShouldReturn404WhenMissing() throws Exception {
        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "title": "Updated title",
                                  "text": "Updated markdown text",
                                  "tags": ["backend", "java"]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteApiPostsIdShouldDeletePostAndReturnOk() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 1 was not found"
                        }
                        """, true));

        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 1 was not found"
                        }
                        """, true));
    }

    @Test
    void deleteApiPostsIdShouldReturn404WhenMissing() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void putApiPostsIdImageShouldStoreImageAndReturnOk() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{11, 22, 33}
        );

        mockMvc.perform(multipart("/api/posts/{id}/image", 1L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(result -> {
                    byte[] content = result.getResponse().getContentAsByteArray();
                    assertEquals(3, content.length);
                    assertEquals(11, content[0]);
                    assertEquals(22, content[1]);
                    assertEquals(33, content[2]);
                });
    }

    @Test
    void putApiPostsIdImageShouldReturn404WhenPostMissing() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{11, 22, 33}
        );

        mockMvc.perform(multipart("/api/posts/{id}/image", 999L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }

    @Test
    void getApiPostsIdImageShouldReturnPngBytes() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsByteArray().length > 0));
    }

    @Test
    void getApiPostsIdImageShouldReturn404WhenPostMissing() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }

    @Test
    void getApiPostsIdCommentsShouldReturnCommentsJson() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                          {
                            "id": 1,
                            "text": "Comment for post 1",
                            "postId": 1
                          }
                        ]
                        """, true));
    }

    @Test
    void getApiPostsIdCommentsShouldReturn404WhenPostMissing() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "message": "Post with id 999 was not found"
                        }
                        """, true));
    }
}
