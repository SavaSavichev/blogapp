package main.service;

import lombok.AllArgsConstructor;
import main.api.response.TagResponse;
import main.model.Post;
import main.model.Tag;
import main.repository.PostRepository;
import main.repository.Tag2PostRepository;
import main.repository.TagRepository;
import org.apache.commons.math3.util.Precision;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@Service
public class TagService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final Tag2PostRepository tag2PostRepository;

    public ResponseEntity<?> getTag() {
        List<Tag> tags = tagRepository.findAll();
        List<String> tagNames = tags.stream().map(Tag::getName).collect(Collectors.toList());
        Map<String, List<TagResponse>> tagsMap = getTagMap(tagNames);
        return ResponseEntity.ok(tagsMap);
    }

    public ResponseEntity<?> getTag(String query) {
        Map<String, List<TagResponse>> tagsResponseMap;
        List<String> tagsList;
        if (query.contains(",")) {
            tagsList = List.of(query.split(","));
            List<String> tagsCleaned = tagsList.stream().map(String::trim).collect(Collectors.toList());
            tagsResponseMap = getTagMap(tagsCleaned);
        } else {
            tagsList = List.of(query);
            tagsResponseMap = getTagMap(tagsList);
        }
        return ResponseEntity.ok(tagsResponseMap);
    }

    private Map<String, List<TagResponse>> getTagMap(List<String> tagNameList) {
        int count = getCount();
        List<Integer> postPerTagList = new ArrayList<>();
        List<Double> partialWeights = new ArrayList<>();

        tagNameList.stream().map(tagRepository::findTagByName)
                .mapToInt(tag -> tag.get().getId())
                .map(tagId -> tag2PostRepository.findPostIdByTagId(tagId).size())
                .forEach(postToTagCount -> {
            postPerTagList.add((postToTagCount));
            double nNWeight = (double) postToTagCount / count;
            partialWeights.add(nNWeight);
        });
        int maxPostsPerTag = postPerTagList.stream().max(Comparator.naturalOrder()).orElse(count);
        double k = 1 / ((double) maxPostsPerTag / count);

        List<Double> tagWeightsList = partialWeights.stream().mapToDouble(nw -> nw * k).boxed()
                .collect(Collectors.toList());

        List<TagResponse> tagResponseList = IntStream.range(0, tagWeightsList.size())
                .mapToObj(i -> new TagResponse(tagNameList.get(i), Precision.round(tagWeightsList.get(i), 2)))
                .collect(Collectors.toList());
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
