package main.service;

import lombok.RequiredArgsConstructor;
import main.api.response.CalendarResponse;
import main.api.response.PostByIdResponse;
import main.api.response.PostResponse;
import main.facade.PostFacade;
import main.model.Post;
import main.model.Tag;
import main.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostFacade postFacade;
    private final TagRepository tagRepository;
    private final Tag2PostRepository tag2PostRepository;


    public ResponseEntity<?> getPosts(Integer offset, Integer limit, String mode) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = getSortedPosts(offset, limit, mode);

        PostResponse postResponse = postFacade.mappingPostResponse(posts, offset, limit);

        return ResponseEntity.ok(postResponse);
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

    public ResponseEntity<?> searchPosts(String query, Integer offset, Integer limit) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = postRepository.findByTextContaining(query.trim());

        PostResponse postResponse = postFacade.mappingPostSearchResponse(posts, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<?> postsByDate(String date, Integer offset, Integer limit) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
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
        if (year > 1970 && year <= convertTimeToYear(currentTimestamp)) {
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
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }
        List<Tag> tags = tagRepository.findTagByName(tag);
        if (tags.isEmpty()) {
            ResponseEntity<?> responseEntity = new ResponseEntity<>("No tag " + tag + " is registered.",
                    HttpStatus.NO_CONTENT);
        } else {
            int tagId = 0;
            for(Tag tag1: tags) {
                tagId = tag1.getId();
            }
            List<Integer> postsIdList = tag2PostRepository.findPostIdByTagId(tagId);
            postsIdList.forEach(id -> posts.add(postRepository.getOne(id))
            );

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            for (Post post: posts) {
                if (post.getIsActive() == 1 && post.getModerationStatus().toString().equals("ACCEPTED")
                        && post.getTimestamp().before(timestamp)) {
                    sortedPost.add(post);
                }
            }
        }
        PostResponse postResponse = postFacade.mappingPostResponse(sortedPost, offset, limit);
        return ResponseEntity.ok(postResponse);
    }

    public Integer convertTimeToYear(Timestamp time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC+3"));
        cal.setTimeInMillis(time.getTime());
        String curTime = String.valueOf(cal.get(Calendar.YEAR));
        return Integer.parseInt(curTime);
    }

    public ResponseEntity<?> getPostById(Integer id) {
        if (postRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("Post with ID = " + id + " not found.", HttpStatus.NOT_FOUND);
        }

        PostByIdResponse postByIdResponse = postFacade.mappingPostById(id);

        return ResponseEntity.ok(postByIdResponse);
    }
}
