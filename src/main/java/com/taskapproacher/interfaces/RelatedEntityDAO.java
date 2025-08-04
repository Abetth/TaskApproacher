package com.taskapproacher.interfaces;

import java.util.List;

public interface RelatedEntityDAO <E, T> {
    List<E> findByRelatedObject (T relatedObject);
}
