package main.facade;

import lombok.RequiredArgsConstructor;
import main.api.response.PostByIdResponse;
import main.api.response.PostResponse;
import main.dto.CommentsDTO;
import main.dto.PostDTO;
import main.dto.UserComDTO;
import main.dto.UserDTO;
import main.model.Comment;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostFacade {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final Tag2PostRepository tag2PostRepository;
    private final TagRepository tagRepository;

    public PostResponse mappingPostResponse (List<Post> posts, Integer offset, Integer limit) {
        List<PostDTO> postDTOS = new ArrayList<>();

        for (Post post : posts) {
            PostDTO postDTO = new PostDTO();
            postDTO.setId(post.getPostId());
            postDTO.setTimestamp(post.getTimestamp().getTime() / 1000);
            int userId = post.getUserId();
            User user = userRepository.getOne(userId);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setName(user.getName());
            postDTO.setUser(userDTO);
            postDTO.setTitle(post.getTitle());
            postDTO.setAnnounce(post.getAnnounce().replaceAll("<(.*?)>", "").replaceAll("[\\p{P}\\p{S}]", ""));
            postDTO.setLikeCount(likeCount(post));
            postDTO.setDislikeCount(dislikeCount(post));
            postDTO.setCommentCount(commentRepository.getCommentCountByPostId(post.getPostId()));
            postDTO.setViewCount(post.getViewCount());
            postDTOS.add(postDTO);
        }
        PostResponse postResponse = new PostResponse();
        postResponse.setCount(posts.size());
        postResponse.setPosts(getOffsetLimit(postDTOS, offset, limit));

        return postResponse;
    }

    public PostResponse mappingPostSearchResponse(List<Post> posts, Integer offset, Integer limit) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<PostDTO> postDTOS = new ArrayList<>();
        List<Post> sortedPosts = new ArrayList<>();

        for (Post post : posts)
            if (post.getIsActive() == 1 && post.getModerationStatus().toString().equals("ACCEPTED")
            && post.getTimestamp().before(timestamp)) {
                sortedPosts.add(post);
            }

        for (Post post : sortedPosts) {
            PostDTO postDTO = new PostDTO();
            postDTO.setId(post.getPostId());
            postDTO.setTimestamp(post.getTimestamp().getTime() / 1000);
            int userId = post.getUserId();
            User user = userRepository.getOne(userId);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setName(user.getName());
            postDTO.setUser(userDTO);
            postDTO.setTitle(post.getTitle());
            postDTO.setAnnounce(post.getAnnounce().replaceAll("<(.*?)>", "").replaceAll("[\\p{P}\\p{S}]", ""));
            postDTO.setLikeCount(likeCount(post));
            postDTO.setDislikeCount(dislikeCount(post));
            postDTO.setCommentCount(commentRepository.getCommentCountByPostId(post.getPostId()));
            postDTO.setViewCount(post.getViewCount());
            postDTOS.add(postDTO);
        }
        PostResponse postResponse = new PostResponse();
        postResponse.setCount(sortedPosts.size());
        postResponse.setPosts(getOffsetLimit(postDTOS, offset, limit));
        return postResponse;
    }

    public PostByIdResponse mappingPostById(Integer postId) {

        PostByIdResponse postByIdResponse = new PostByIdResponse();

        Post post = postRepository.getOne(postId);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        postByIdResponse.setId(post.getPostId());
        postByIdResponse.setTimestamp(post.getTimestamp().getTime() / 1000);
        postByIdResponse.setActive(true);

        User user = userRepository.getOne(post.getUserId());
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setId(user.getUserId());

        postByIdResponse.setUser(userDTO);
        postByIdResponse.setTitle(post.getTitle());
        postByIdResponse.setText(post.getText());
        postByIdResponse.setLikeCount(likeCount(post));
        postByIdResponse.setDislikeCount(dislikeCount(post));
        postByIdResponse.setViewCount(post.getViewCount());

        List<Comment> postCommentList = commentRepository.findCommentsByPostId(postId);
        CommentsDTO commentsDTO = new CommentsDTO();
        List<CommentsDTO> commentsDTOS = new ArrayList<>();

        for (Comment comment : postCommentList) {
            commentsDTO.setId(comment.getCommentId());
            commentsDTO.setTimestamp(comment.getTime().getTime() / 1000);
            commentsDTO.setText(comment.getText());
            User commentUser = userRepository.findById(comment.getUserId()).orElseThrow();
            UserComDTO userComDTO = new UserComDTO();
            userComDTO.setId(commentUser.getUserId());
            userComDTO.setName(commentUser.getName());
            userComDTO.setPhoto(commentUser.getPhoto());
            commentsDTO.setUser(userComDTO);
            commentsDTOS.add(commentsDTO);
        }
        postByIdResponse.setComments(commentsDTOS);

        if (post.getIsActive() == 1 && post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
                post.getTimestamp().getTime() < Timestamp.valueOf(LocalDateTime.now()).getTime()) {
            List<Integer> tagsIdList = tag2PostRepository.findTagIdsByPostId(postId);
            List<String> tagNames = new ArrayList<>();

            if (!tagsIdList.isEmpty()) {
                tagNames = tagsIdList.stream()
                        .map(t -> tagRepository.getOne(t).getName())
                        .collect(Collectors.toList());
            }
            postByIdResponse.setTags(tagNames);
        }
        return postByIdResponse;
    }

    private Integer likeCount(Post post) {
        return voteRepository.findCountVotesByPostId(post.getPostId(), 1).orElse(0);
    }

    private Integer dislikeCount(Post post) {
        return voteRepository.findCountVotesByPostId(post.getPostId(), -1).orElse(0);
    }

    public List<PostDTO> getOffsetLimit(List<PostDTO> list,
                                        Integer offset, Integer limit) {
        List<PostDTO> listResult;
        if (offset > list.size() || offset > limit) {
            return new ArrayList<>();
        }
        if (limit + offset <= list.size()) {
            listResult = list.subList(offset, offset + limit);
        } else {
            listResult = list.subList(offset, list.size());
        }
        return listResult;
    }
}
