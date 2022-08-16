package main.facade;

import lombok.RequiredArgsConstructor;
import main.api.response.PostResponse;
import main.dto.PostDTO;
import main.dto.UserDTO;
import main.model.Post;
import main.model.User;
import main.repository.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PostFacade {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

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

        // УТОЧНИТЬ ПРОВЕРКУ ВРЕМЕНИ!!!

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
