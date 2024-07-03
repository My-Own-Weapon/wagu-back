package com.chimaera.wagubook.service;

import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.StoreQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreQueryRepository storeRepository;
    private final PostRepository postRepository;
    public List<Store> getStoresByScreen(String left, String right, String up, String down) {
        return storeRepository.findAllByScreen(left,right,up,down);
    }


    public List<Post> getAllPostsByStore(Long storeId) {
        return postRepository.findAllByStoreId(storeId);
    }
}
