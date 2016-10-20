package org.dbpedia.topics.dataset.models.impl;

import org.dbpedia.topics.dataset.models.Instance;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by wlu on 31.05.16.
 */
@Entity(value = "and", noClassnameStored = true)
public class ANDArticle extends Instance {
    public ANDArticle() {
    }
}
