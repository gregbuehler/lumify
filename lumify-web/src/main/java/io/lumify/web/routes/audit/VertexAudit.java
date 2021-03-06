package io.lumify.web.routes.audit;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.audit.Audit;
import io.lumify.core.model.audit.AuditRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VertexAudit extends BaseRequestHandler {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;

    @Inject
    public VertexAudit(
            final AuditRepository auditRepository,
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String graphVertexId = getAttributeString(request, "graphVertexId");
        User user = getUser(request);
        ModelUserContext modelUserContext = userRepository.getModelUserContext(getAuthorizations(request, user), getActiveWorkspaceId(request));
        Iterable<Audit> rows = auditRepository.findByRowStartsWith(graphVertexId, modelUserContext);

        JSONObject results = new JSONObject();
        JSONArray audits = new JSONArray();
        for (Audit audit : rows) {
            JSONObject data = audit.toJson();
            audits.put(data);
        }
        results.put("auditHistory", audits);
        respondWithJson(response, results);
    }
}
