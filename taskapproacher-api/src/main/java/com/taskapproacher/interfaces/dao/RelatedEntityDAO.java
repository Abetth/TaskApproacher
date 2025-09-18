package com.taskapproacher.interfaces.dao;

import java.util.List;

public interface RelatedEntityDAO <E, UUID> {
    List<E> findRelatedEntitiesByID(UUID findBy);
}
