package com.reljicd.controller;

import com.reljicd.model.Post;
import com.reljicd.model.User;
import com.reljicd.service.PostService;
import com.reljicd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.GET)
    public String newPost(Model model) {
        Optional<User> defaultAuthor = getDefaultAuthor();

        Post post = new Post();
        defaultAuthor.ifPresent(post::setUser);

        model.addAttribute("post", post);
        return "/postForm";
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.POST)
    public String createNewPost(@Valid Post post,
                                BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/postForm";
        }

        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return "/postForm";
        }

        if (post.getBody() == null || post.getBody().trim().isEmpty()) {
            return "/postForm";
        }

        Optional<User> defaultAuthor = getDefaultAuthor();
        if (!defaultAuthor.isPresent()) {
            return "/error";
        }

        post.setUser(defaultAuthor.get());
        postService.save(post);
        return "redirect:/home";
    }

    @RequestMapping(value = "/editPost/{id}", method = RequestMethod.GET)
    public String editPostWithId(@PathVariable Long id,
                                 Model model) {

        Optional<Post> optionalPost = postService.findForId(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);
            return "/postForm";
        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.GET)
    public String getPostWithId(@PathVariable Long id,
                                Model model) {

        Optional<Post> optionalPost = postService.findForId(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);
            return "/post";
        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.DELETE)
    public String deletePostWithId(@PathVariable Long id) {

        Optional<Post> optionalPost = postService.findForId(id);

        if (optionalPost.isPresent()) {
            postService.delete(optionalPost.get());
            return "redirect:/home";
        } else {
            return "/error";
        }
    }

    private Optional<User> getDefaultAuthor() {
        Optional<User> author = userService.findByUsername("admin");
        if (author.isPresent()) {
            return author;
        }

        author = userService.findByUsername("user");
        if (author.isPresent()) {
            return author;
        }

        User fallback = new User();
        fallback.setUsername("admin");
        fallback.setPassword("admin");
        fallback.setEmail("admin@localhost");
        fallback.setName("Admin");
        fallback.setLastName("User");
        fallback.setActive(1);

        try {
            return Optional.of(userService.save(fallback));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
