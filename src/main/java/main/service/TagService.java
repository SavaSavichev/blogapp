package main.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import main.api.response.TagResponse;
import main.model.Post;
import main.model.Tag;
import main.repository.PostRepository;
import main.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TagService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public ResponseEntity<?> getTag() {
        List<Tag> tags = tagRepository.findAll();
        List<String> tagNames = tags.stream().map(Tag::getName).collect(Collectors.toList());
        Map<String, List<TagResponse>> tagsMap = getTagMap(tagNames);
        return new ResponseEntity<>(tagsMap, HttpStatus.OK);
    }

    private Map<String, List<TagResponse>> getTagMap(List<String> tagNameList) {
        List<Post> postList = (List<Post>) postRepository.findAllActivePosts();
        int count = getCount();
        List<Integer> postsPerTagList = new ArrayList<>();
        for (String t : tagNameList) {
            postsPerTagList.add((int) postList.stream().filter(p -> p.getText().contains(t)).count());
        }
        int maxPostsPerTag = postsPerTagList.stream().max(Comparator.naturalOrder()).orElse(count);
        List<Double> partialWeights = postsPerTagList.stream().map(t -> (double) t / maxPostsPerTag)
                .collect(Collectors.toList());
        List<TagResponse> tagResponseList = new ArrayList<>();
        for (int i = 0; i < partialWeights.size(); i++) {
            tagResponseList.add(new TagResponse(tagNameList.get(i), partialWeights.get(i)));
        }
        return Map.of("tags", tagResponseList);
    }

    public Integer getCount() {
        int count;
        try {
            List<Post> postList = (List<Post>) postRepository.findAllActivePosts();
            count = postList.size();
        } catch (NullPointerException ex) {
            count = 0;
        }
        return count;
    }
}
