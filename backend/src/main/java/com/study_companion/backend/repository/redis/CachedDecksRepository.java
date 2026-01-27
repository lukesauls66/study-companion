package com.study_companion.backend.repository.redis;

import com.study_companion.backend.model.redis.CachedDecks;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface CachedDecksRepository extends CrudRepository<CachedDecks, UUID> {

}