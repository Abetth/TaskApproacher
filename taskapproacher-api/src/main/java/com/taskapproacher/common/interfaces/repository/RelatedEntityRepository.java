package com.taskapproacher.common.interfaces.repository;

import java.util.List;

public interface RelatedEntityRepository<E, UUID> {
    List<E> findRelatedEntitiesByID(UUID findBy);
}
