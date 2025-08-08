package com.taskapproacher.interfaces;

import java.util.List;

public interface RelatedEntityDAO <E, UUID> {
    List<E> findRelatedEntitiesByUUID(UUID findBy);
}
