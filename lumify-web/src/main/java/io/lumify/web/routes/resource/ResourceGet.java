package io.lumify.web.routes.resource;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.ontology.Concept;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ResourceGet extends BaseRequestHandler {
    private final OntologyRepository ontologyRepository;

    @Inject
    public ResourceGet(
            final OntologyRepository ontologyRepository,
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
        this.ontologyRepository = ontologyRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String id = getAttributeString(request, "id");

        Concept concept = ontologyRepository.getConceptByIRI(id);
        InputStream glyphIconIn = concept.getGlyphIcon();

        if (glyphIconIn == null) {
            respondWithNotFound(response);
            return;
        }

        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        IOUtils.copy(glyphIconIn, imgOut);

        byte[] rawImg = imgOut.toByteArray();

        // TODO change content type if we use this route for more than getting glyph icons
        response.setContentType("image/png");
        ServletOutputStream out = response.getOutputStream();
        out.write(rawImg);
        out.close();
    }
}
