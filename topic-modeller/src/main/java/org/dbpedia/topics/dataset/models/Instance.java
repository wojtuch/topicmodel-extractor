package org.dbpedia.topics.dataset.models;

import org.dbpedia.utils.annotation.models.SpotlightAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of the single document used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public abstract class Instance {

    protected String text;
    protected String uri;
    protected SpotlightAnnotation spotlightAnnotation;
    protected List<String> hypernyms = new ArrayList<>();
    protected List<String> lemmas = new ArrayList<>();

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

    /**
     * Returns the annotation of this document.
     * @return
     */
    public SpotlightAnnotation getSpotlightAnnotation() {
        return spotlightAnnotation;
    }

    /**
     * Sets the annotation of this document.
     * @param spotlightAnnotation
     */
    public void setSpotlightAnnotation(SpotlightAnnotation spotlightAnnotation) {
        this.spotlightAnnotation = spotlightAnnotation;
    }

    /**
     * Gets the hypernyms (objects with the property http://purl.org/linguistics/gold/hypernym) of the spotted entities
     * within this document.
     * @return
     */
    public List<String> getHypernyms() {
        return hypernyms;
    }

    /**
     * Sets the hypernyms (objects with the property http://purl.org/linguistics/gold/hypernym) of the spotted entities
     * within this document.
     * @param hypernyms
     */
    public void setHypernyms(List<String> hypernyms) {
        this.hypernyms = hypernyms;
    }

    /**
     * Adds a hypernym to the current list.
     * @param hypernym
     * @return as specified by List.add()
     */
    public boolean addHypernym(String hypernym) {
        return this.hypernyms.add(hypernym);
    }

    /**
     * Gets the lemmas of the text of this document.
     * @return
     */
    public List<String> getLemmas() {
        return lemmas;
    }

    /**
     * Sets the lemmas of the text of this document.
     * @return
     */
    public void setLemmas(List<String> lemmas) {
        this.lemmas = lemmas;
    }
}
