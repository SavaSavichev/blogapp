package main.service;

import lombok.RequiredArgsConstructor;
import main.api.request.CommentRequest;
import main.api.request.LikeDislikeRequest;
import main.api.request.PostRequest;
import main.api.response.*;
import main.facade.PostFacade;
import main.model.*;
import main.model.enums.ModerationStatus;
import main.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostFacade postFacade;
    private final TagRepository tagRepository;
    private final Tag2PostRepository tag2PostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final UserService userService;

    @Value("${config.imageHeight}")
    private Integer imageHeight;
    @Value("${config.imageWidth}")
    private Integer imageWidth;
    private Integer startYear = 1970;

    public ResponseEntity<?> getPosts(Integer offset, Integer limit, String mode) {
        if (offset > limit) {
            return new ResponseEntity<>("Неправильные входные параметры!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = getSortedPosts(offset, limit, mode);

        PostResponse postResponse = postFacade.mappingPostResponse(posts, offset, limit);

        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<?> searchPosts(String query, Integer offset, Integer limit) {
        if (offset > limit) {
            return new ResponseEntity<>("Неправильные входные параметры!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = postRepository.findByTextContaining(query.trim());

        PostResponse postResponse = postFacade.mappingPostSearchResponse(posts, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<?> postsByDate(String date, Integer offset, Integer limit) {
        if (offset > limit) {
            return new ResponseEntity<>("Неправильные входные параметры!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = postRepository.findAllActivePosts()
                .stream()
                .filter(post -> post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        .equals(date)).collect(Collectors.toList());

        PostResponse postResponse = postFacade.mappingPostResponse(posts, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<?> calendarPosts(Integer year) {
        List<Integer> years;
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        List<Timestamp> timestamps;
        Map<String, Integer> posts = new LinkedHashMap<>();
        int postCountAtDate;
        List<Post> postsList = postRepository.findAllActivePosts().stream()
                .sorted(Comparator.comparing(Post::getTimestamp))
                .collect(Collectors.toList());
        years = postsList.stream()
                .map(p -> convertTimeToYear(p.getTimestamp()))
                .distinct()
                .collect(Collectors.toList());
        if (year > startYear && year <= convertTimeToYear(currentTimestamp)) {
            timestamps = postRepository.findAllActivePosts().stream()
                    .map(Post::getTimestamp)
                    .filter(t_stamp -> convertTimeToYear(t_stamp).equals(year))
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            int currentYear = LocalDate.now().getYear();
            timestamps = postsList.stream().
                    map(Post::getTimestamp).
                    filter(t_stamp -> convertTimeToYear(t_stamp).equals(currentYear)).
                    distinct().
                    collect(Collectors.toList());
        }
        timestamps.sort(Comparator.naturalOrder());
        for (Timestamp d : timestamps) {
            postCountAtDate = (int) postsList.stream()
                    .filter(p -> (p.getTimestamp().toInstant()
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()).equals(d.toInstant()
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()))
                    .count();
            posts.put(String.valueOf(d.toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()), postCountAtDate);
        }
        CalendarResponse calendarResponse = new CalendarResponse();
        calendarResponse.setYears(years);
        calendarResponse.setPosts(posts);
        return ResponseEntity.ok(calendarResponse);
    }

    public ResponseEntity<?> postsByTag(String tag, Integer offset, Integer limit) {
        List<Post> sortedPost = new ArrayList<>();
        List<Post> posts = new ArrayList<>();
        if (offset > limit) {
            return ResponseEntity.badRequest().body("Неправильные входные параметры!");
        }
        List<Tag> tags = tagRepository.findPostsByTagName(tag);
        if (tags.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Тэг " + tag + " не зарегестрирован!");
        } else {
            int tagId = 0;
            for (Tag tag1 : tags) {
                tagId = tag1.getId();
            }
            List<Integer> postsIdList = tag2PostRepository.findPostIdByTagId(tagId);
            postsIdList.forEach(id -> posts.add(postRepository.getOne(id))
            );

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            for (Post post : posts) {
                if (post.getIsActive() == 1 && post.getModerationStatus().toString().equals("ACCEPTED")
                        && post.getTimestamp().before(timestamp)) {
                    sortedPost.add(post);
                }
            }
        }
        PostResponse postResponse = postFacade.mappingPostResponse(sortedPost, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<?> postById(Integer id) {
        if (postRepository.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пост с ID = " + id + " не найден!");
        }
        PostByIdResponse postByIdResponse = postFacade.mappingPostById(id);
        return ResponseEntity.ok(postByIdResponse);
    }

    public ResponseEntity<?> myPosts(Principal principal, Integer offset, Integer limit, String status) {
        if (offset > limit) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неправильные входные параметры!");
        }

        Optional<User> user = userRepository.findOneByEmail(principal.getName());
        List<Post> posts = new ArrayList<>();
        if (user.isPresent()) {
            int userId = user.get().getUserId();
            posts = getSortedMyPosts(userId, offset, limit, status);
        }
        return ResponseEntity.ok(postFacade.mappingPostResponse(posts, offset, limit));
    }

    public List<Post> getSortedPosts(Integer offset, Integer limit, String mode) {
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        switch (mode) {
            case "popular":
                posts = postRepository.getPopularPosts(pageRequest);
                break;
            case "best":
                posts = postRepository.getBestPosts(pageRequest);
                break;
            case "early":
                posts = postRepository.getEarlyPosts(pageRequest);
                break;
            default:
                posts = postRepository.getRecentPosts(pageRequest);
                break;
        }
        return posts.toList();
    }

    public List<Post> getSortedPostsForModeration(Integer userId, Integer offset, Integer limit, String status) {
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        switch (status) {
            case "declined":
                posts = postRepository.getDeclinedPostsForModeration(userId, pageRequest);
                break;
            case "accepted":
                posts = postRepository.getAcceptedPostForModeration(userId, pageRequest);
                break;
            default:
                posts = postRepository.getNewPostForModeration(userId, pageRequest);
                break;
        }
        return posts.toList();
    }

    public List<Post> getSortedMyPosts(Integer userId, Integer offset, Integer limit, String status) {
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        switch (status) {
            case "pending":
                posts = postRepository.getPendingPosts(userId, pageRequest);
                break;
            case "declined":
                posts = postRepository.getDeclinedPosts(userId, pageRequest);
                break;
            case "published":
                posts = postRepository.getPublishedPosts(userId, pageRequest);
                break;
            default:
                posts = postRepository.getInactivePosts(userId, pageRequest);
                break;
        }
        return posts.toList();
    }

    public Integer convertTimeToYear(Timestamp time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC+3"));
        cal.setTimeInMillis(time.getTime());
        String curTime = String.valueOf(cal.get(Calendar.YEAR));
        return Integer.parseInt(curTime);
    }

    public ResponseEntity<?> createPost(PostRequest postRequest, Principal principal) {
        User currentUser = userRepository.findOneByEmail(principal.getName()).orElse(null);
        Post post = new Post();
        ResultResponse resultResponse = new ResultResponse();
        Map<String, String> errors = textValidate(postRequest.getTitle(), postRequest.getText());
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

        if (!errors.isEmpty()) {
            resultResponse.setResult(false);
            resultResponse.setErrors(errors);
            return ResponseEntity.ok(resultResponse);
        }

        if (postRequest.getTimestamp() <= currentTimestamp.getTime() / 1000) {
            post.setTimestamp(currentTimestamp);
        } else {
            post.setTimestamp(new Timestamp(postRequest.getTimestamp() * 1000));
        }
        assert currentUser != null;
        post.setIsActive(postRequest.getActive())
                .setTitle(postRequest.getTitle().replaceAll("<(.*?)>", "").replaceAll("[\\p{P}\\p{S}]", ""))
                .setModerationStatus(ModerationStatus.NEW)
                .setText(postRequest.getText())
                .setUserId(currentUser.getUserId())
                .setViewCount(0);
        List<Integer> moderatorIds = userRepository.getModeratorIds();
        int moderatorId = moderatorIds.get((int) (Math.random() * moderatorIds.size()));
        post.setModeratorId(moderatorId);

        postRepository.save(post);
        saveTags(post, postRequest);
        resultResponse.setResult(true);

        return ResponseEntity.ok(resultResponse);
    }

    public ResponseEntity<?> changePost(int id, PostRequest postRequest) {
        Optional<Post> postOptional = postRepository.findById(id);
        ResultResponse resultResponse = new ResultResponse();
        Map<String, String> errors = textValidate(postRequest.getTitle(), postRequest.getText());
        if (!errors.isEmpty()) {
            resultResponse.setResult(false);
            resultResponse.setErrors(errors);
            return ResponseEntity.ok(resultResponse);
        }
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultResponse);
        }
        Post post = postOptional.get();
        User user = userRepository.getOne(post.getUserId());
        post.setText(postRequest.getText())
                .setTitle(postRequest.getTitle())
                .setIsActive(postRequest.getActive());
        long currentTime = Instant.now().toEpochMilli();
        if (postRequest.getTimestamp() <= currentTime) {
            post.setTimestamp(new Timestamp(currentTime));
        }
        if (user.getIsModerator() != 1) {
            post.setModerationStatus(ModerationStatus.NEW);
        }
        postRepository.save(post);
        saveTags(post, postRequest);
        ResultResponse response = new ResultResponse();
        response.setResult(true);
        return ResponseEntity.ok(response);
    }

    private void saveTags(Post post, PostRequest postRequest) {
        for (String tag : postRequest.getTags()) {
            Tag tagModel = tagRepository.findTagByName(tag).orElse(null);
            if (tagModel == null) {
                tagModel = new Tag();
                tagModel.setName(tag.toUpperCase());
                tagRepository.save(tagModel);
            }
            Tag2Post tag2Post = new Tag2Post();
            tag2Post.setPostId(post.getPostId());
            tag2Post.setTagId(tagModel.getId());
            tag2PostRepository.save(tag2Post);
        }
    }

    public ResponseEntity<?> addComment(CommentRequest commentRequest, Principal principal) {
        User user = userRepository.findOneByEmail(principal.getName()).orElse(null);
        assert user != null;
        Integer userId = user.getUserId();
        Integer postId = commentRequest.getPostId();
        Comment postComment = new Comment();
        Map<String, String> errors = new LinkedHashMap<>();
        ResultResponse resultResponse = new ResultResponse();

        if (commentRequest.getText().length() < 5 || commentRequest.getText().length() > 250) {
            errors.put("text", "Текст комментария не задан или слишком короткий!");
        }
        if (errors.isEmpty()) {
            if (commentRequest.getParentId() != null) {
                postComment.setParent_id(commentRequest.getParentId());
            }
            postComment.setPost_id(postId);
            postComment.setPost(postRepository.getOne(postId));
            postComment.setText(commentRequest.getText());
            postComment.setTime(Timestamp.valueOf(now()));
            postComment.setUserId(userId);
            commentRepository.saveAndFlush(postComment);
            resultResponse.setId(postComment.getCommentId());
        } else {
            resultResponse.setResult(false);
            resultResponse.setErrors(errors);
        }
        return ResponseEntity.ok(resultResponse);
    }

    public ResponseEntity<?> addLikeDislike(LikeDislikeRequest likeDislikeRequest, Principal principal, Integer value) {
        Vote vote;
        User user = userRepository.findOneByEmail(principal.getName()).orElse(null);
        assert user != null;
        Integer userId = user.getUserId();
        Integer postId = likeDislikeRequest.getPostId();
        Optional<Vote> postVoteOptional = voteRepository.getOneByPostAndUser(postId, userId);
        ResultResponse resultResponse = new ResultResponse();

        if (postVoteOptional.isPresent()) {
            if (postVoteOptional.get().getValue().equals(value)) {
                resultResponse.setResult(false);
            } else {
                vote = postVoteOptional.get();
                vote.setValue(value);
                voteRepository.save(vote);
                resultResponse.setResult(true);
            }
            return ResponseEntity.ok(resultResponse);
        }
        Vote newVote = createPostVote(postId, value, userId);
        voteRepository.save(newVote);
        resultResponse.setResult(true);
        return ResponseEntity.ok(resultResponse);
    }

    private Vote createPostVote(Integer postId, Integer value, Integer userId) {
        Vote postVote = new Vote();
        postVote.setPost(postRepository.getOne(postId));
        postVote.setPostId(postId);
        postVote.setTime(Timestamp.valueOf(now()));
        postVote.setUserId(userId);
        postVote.setValue(value);
        return postVote;
    }

    public ResponseEntity<?> addImage(MultipartFile image) throws IOException {
        int maxImageSize = 1_000_000;
        Map<String, Object> errors = new LinkedHashMap<>();
        Map<String, Object> responseMap = new LinkedHashMap<>();

        if (image.getSize() <= maxImageSize) {
            File convertedFile = userService.saveImage(image, imageHeight, imageWidth);
            String photoDestination = StringUtils.cleanPath(convertedFile.getPath());
            if (!photoDestination.endsWith("jpg") && !photoDestination.endsWith("png")) {
                errors.put("image", "Неправильный формат фотографии!");
                responseMap.put("result", false);
                responseMap.put("errors", errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            return ResponseEntity.ok("/" + photoDestination);
        } else {
            errors.put("image", "Размер файла превышает допустимый размер");
            responseMap.put("result", false);
            responseMap.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    public ResponseEntity<?> updateModeration(Integer postId, String decision, Principal principal) {
        ResultResponse resultResponse = new ResultResponse();
        Optional<User> currentUser = userRepository.findOneByEmail(principal.getName());
        User user = currentUser.get();
        if (user.getIsModerator() == 1) {
            Optional<Post> optionalPost = postRepository.findById(postId);
            if (optionalPost.isEmpty()) {
                resultResponse.setResult(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultResponse);
            }
            Post post = optionalPost.get();
            if (decision.equals("accept")) {
                post.setModerationStatus(ModerationStatus.ACCEPTED);
                post.setModeratorId(user.getUserId());
                postRepository.save(post);
                resultResponse.setResult(true);
            } else if (decision.equals("decline")) {
                post.setModerationStatus(ModerationStatus.DECLINED);
                post.setModeratorId(user.getUserId());
                postRepository.save(post);
                resultResponse.setResult(true);
            }
        } else {
            resultResponse.setResult(false);
        }
        return ResponseEntity.ok(resultResponse);
    }

    public ResponseEntity<?> postsForModeration(Integer offset, Integer limit, String status, Principal principal) {
        if (offset > limit) {
            return ResponseEntity.badRequest().body("Неправильные входные параметры!");
        }
        Optional<User> currentUser = userRepository.findOneByEmail(principal.getName());
        User user = currentUser.get();

        List<Post> posts = getSortedPostsForModeration(user.getUserId(), offset, limit, status);
        PostResponse postResponse = postFacade.mappingPostResponse(posts, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    private Map<String, String> textValidate(String title, String text) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (title.length() < 3) {
            errors.put("title", "Заголовок слишком короткий");
        } else if (title.length() > 100) {
            errors.put("title", "Заголовок слишком длинный!");
        }
        if (text.length() < 30) {
            errors.put("text", "Текст публикации слишком короткий");
        } else if (text.length() > 1500) {
            errors.put("text", "Текст публикации слишком длинный!");
        }
        return errors;
    }
}
