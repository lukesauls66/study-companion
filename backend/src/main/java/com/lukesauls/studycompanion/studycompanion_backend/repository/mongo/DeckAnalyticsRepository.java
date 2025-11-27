package com.lukesauls.studycompanion.studycompanion_backend.repository.mongo;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.lukesauls.studycompanion.studycompanion_backend.model.mongo.DeckAnalytics;

public interface DeckAnalyticsRepository extends MongoRepository<DeckAnalytics, UUID> {
    
}
