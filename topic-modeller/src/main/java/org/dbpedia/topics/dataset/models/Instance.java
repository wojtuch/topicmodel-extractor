package org.dbpedia.topics.dataset.models;

/**
 * Representation of the single document used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public abstract class Instance {

    private String text;
    private String uri;

    /**
     * Returns the URI of this document.
     * @return
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the uri of this document.
     * @param uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the text content of this document.
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of this document.
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
}
