package org.dbpedia.topics.dataset.models.impl;

import org.dbpedia.topics.dataset.models.Instance;

/**
 * Created by wlu on 26.05.16.
 */
public class DBpediaAbstract implements Instance {

    private String text;
    private String uri;

    public DBpediaAbstract() {
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
