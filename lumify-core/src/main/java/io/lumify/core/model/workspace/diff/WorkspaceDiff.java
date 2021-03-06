package io.lumify.core.model.workspace.diff;

import com.google.inject.Inject;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.Workspace;
import io.lumify.core.model.workspace.WorkspaceEntity;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.GraphUtil;
import org.securegraph.*;

import java.util.ArrayList;
import java.util.List;

import static org.securegraph.util.IterableUtils.toList;

public class WorkspaceDiff {
    private final Graph graph;
    private final UserRepository userRepository;

    @Inject
    public WorkspaceDiff(
            final Graph graph,
            final UserRepository userRepository) {
        this.graph = graph;
        this.userRepository = userRepository;
    }

    public List<DiffItem> diff(Workspace workspace, List<WorkspaceEntity> workspaceEntities, List<Edge> workspaceEdges, User user) {
        Authorizations authorizations = userRepository.getAuthorizations(user, WorkspaceRepository.VISIBILITY_STRING, workspace.getId());

        List<DiffItem> result = new ArrayList<DiffItem>();
        for (WorkspaceEntity workspaceEntity : workspaceEntities) {
            List<DiffItem> entityDiffs = diffWorkspaceEntity(workspace, workspaceEntity, authorizations);
            if (entityDiffs != null) {
                result.addAll(entityDiffs);
            }
        }

        for (Edge workspaceEdge : workspaceEdges) {
            List<DiffItem> entityDiffs = diffEdge(workspace, workspaceEdge);
            if (entityDiffs != null) {
                result.addAll(entityDiffs);
            }
        }

        return result;
    }

    private List<DiffItem> diffEdge(Workspace workspace, Edge edge) {
        List<DiffItem> result = new ArrayList<DiffItem>();

        SandboxStatus sandboxStatus = GraphUtil.getSandboxStatus(edge, workspace.getId());
        if (sandboxStatus != SandboxStatus.PUBLIC) {
            result.add(new EdgeDiffItem(edge, sandboxStatus));
        }

        diffProperties(workspace, edge, result);

        return result;
    }

    public List<DiffItem> diffWorkspaceEntity(Workspace workspace, WorkspaceEntity workspaceEntity, Authorizations authorizations) {
        List<DiffItem> result = new ArrayList<DiffItem>();

        Vertex entityVertex = this.graph.getVertex(workspaceEntity.getEntityVertexId(), authorizations);

        // vertex can be null if the user doesn't have access to the entity
        if (entityVertex == null) {
            return null;
        }

        SandboxStatus sandboxStatus = GraphUtil.getSandboxStatus(entityVertex, workspace.getId());
        if (sandboxStatus != SandboxStatus.PUBLIC) {
            result.add(new VertexDiffItem(entityVertex, sandboxStatus, workspaceEntity.isVisible()));
        }

        diffProperties(workspace, entityVertex, result);

        return result;
    }

    private void diffProperties(Workspace workspace, Element element, List<DiffItem> result) {
        List<Property> properties = toList(element.getProperties());
        SandboxStatus[] propertyStatuses = GraphUtil.getPropertySandboxStatuses(properties, workspace.getId());
        for (int i = 0; i < properties.size(); i++) {
            if (propertyStatuses[i] != SandboxStatus.PUBLIC) {
                Property property = properties.get(i);
                Property existingProperty = findExistingProperty(properties, propertyStatuses, property);
                result.add(new PropertyDiffItem(element, property, existingProperty, propertyStatuses[i]));
            }
        }
    }

    private Property findExistingProperty(List<Property> properties, SandboxStatus[] propertyStatuses, Property workspaceProperty) {
        for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);
            if (property.getName().equals(workspaceProperty.getName())
                    && property.getKey().equals(workspaceProperty.getKey())
                    && propertyStatuses[i] == SandboxStatus.PUBLIC) {
                return property;
            }
        }
        return null;
    }
}
