package com.lukesauls.studycompanion.studycompanion_backend.repository.redis;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.CachedDecks;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface CachedDecksRepository extends CrudRepository<CachedDecks, UUID> {

}