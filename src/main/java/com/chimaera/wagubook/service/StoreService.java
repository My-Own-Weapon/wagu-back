package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.StoreResponse;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PostRepository postRepository;
    public List<StoreResponse> getStoresByScreen(String left, String right, String up, String down) {
        return storeRepository.findAllByScreen(left,right,up,down)
                .stream()
                .map(s -> (new StoreResponse(s)))
                .collect(Collectors.toList());
    }


    public List<Post> getAllPostsByStore(Long storeId) {
        return postRepository.findAllByStoreId(storeId);
    }

    public Store findByStoreId(String store_id) {
        return storeRepository.findById(Long.parseLong(store_id)).get();
    }
}
