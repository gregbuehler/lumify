package io.lumify.core.model.ontology;

public class InMemoryRelationship extends Relationship {
    private String relationshipIRI;
    private String displayName;

    protected InMemoryRelationship(String relationshipIRI, String displayName, String sourceConceptIRI, String destConceptIRI) {
        super(sourceConceptIRI, destConceptIRI);
        this.relationshipIRI = relationshipIRI;
        this.displayName = displayName;
    }

    @Override
    public String getIRI() {
        return relationshipIRI;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
