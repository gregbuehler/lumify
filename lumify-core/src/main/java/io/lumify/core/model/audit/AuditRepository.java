package io.lumify.core.model.audit;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.bigtable.model.Repository;
import com.altamiracorp.bigtable.model.Row;
import io.lumify.core.user.User;
import org.securegraph.Edge;
import org.securegraph.Vertex;
import org.securegraph.Visibility;
import org.securegraph.mutation.ElementMutation;

import java.util.List;
import java.util.Map;

public abstract class AuditRepository extends Repository<Audit> {
    public AuditRepository(ModelSession modelSession) {
        super(modelSession);
    }

    public abstract Audit fromRow(Row row);

    public abstract Row toRow(Audit audit);

    public abstract String getTableName();

    public abstract Audit auditVertex(AuditAction auditAction, Object vertexId, String process, String comment, User user, FlushFlag flushFlag, Visibility visibility);

    public abstract Audit auditEntityProperty(AuditAction action, Object id, String propertyKey, String propertyName, Object oldValue, Object newValue,
                                              String process, String comment, Map<String, Object> metadata, User user,
                                              Visibility visibility);

    public abstract List<Audit> auditRelationship(AuditAction action, Vertex sourceVertex, Vertex destVertex, Edge edge, String process,
                                                  String comment, User user, Visibility visibility);

    public abstract List<Audit> auditRelationshipProperty(AuditAction action, String sourceId, String destId, String propertyKey, String propertyName,
                                                          Object oldValue, Object newValue, Edge edge, String process, String comment, User user,
                                                          Visibility visibility);

    public abstract void auditVertexElementMutation(AuditAction action, ElementMutation<Vertex> vertexElementMutation, Vertex vertex, String process,
                                                    User user, Visibility visibility);

    public abstract void auditEdgeElementMutation(AuditAction action, ElementMutation<Edge> edgeElementMutation, Edge edge, Vertex sourceVertex, Vertex destVertex, String process,
                                                  User user, Visibility visibility);

    public abstract void updateColumnVisibility (Audit audit, Visibility originalEdgeVisibility, String visibilityString, FlushFlag flushFlag);
}
