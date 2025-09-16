package pl.coderslab.tags;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service

public class TagService {

    private final TagRepository tagRepository;
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id).orElse(null);
    }

    public Set<Tag> getTagsByIds(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> tagList = tagRepository.findAllById(tagIds);
        return new HashSet<>(tagList);
    }

    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }
}
