package io.lumify.web.devTools.user;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserAddAuthorization extends BaseRequestHandler {
    @Inject
    public UserAddAuthorization(
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String auth = getRequiredParameter(request, "auth");

        User user = getUser(request);
        if (user == null) {
            respondWithNotFound(response);
            return;
        }

        getUserRepository().addAuthorization(user, auth);

        respondWithJson(response, getUserRepository().toJsonWithAuths(user));
    }
}
