package io.lumify.web.routes.admin;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import org.securegraph.util.FilterIterable;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.semanticweb.owlapi.model.IRI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.securegraph.util.IterableUtils.toList;

public class AdminUploadOntology extends BaseRequestHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(AdminUploadOntology.class);
    private final OntologyRepository ontologyRepository;

    @Inject
    public AdminUploadOntology(
            final OntologyRepository ontologyRepository,
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
        this.ontologyRepository = ontologyRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String documentIRIString = getRequiredParameter(request, "documentIRI");

        List<Part> files = toList(getFiles(request));
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }
        Part file = files.get(0);

        File tempFile = File.createTempFile("ontologyUpload", ".bin");
        writeToTempFile(file, tempFile);

        IRI documentIRI = IRI.create(documentIRIString);

        User user = getUser(request);
        ontologyRepository.writePackage(tempFile, documentIRI);

        tempFile.delete();

        respondWithPlaintext(response, "OK");
    }

    private Iterable<Part> getFiles(HttpServletRequest request) throws IOException, ServletException {
        return new FilterIterable<Part>(request.getParts()) {
            @Override
            protected boolean isIncluded(Part part) {
                return part.getName().equals("file");
            }
        };
    }

    private void writeToTempFile(Part file, File tempFile) throws IOException {
        InputStream in = file.getInputStream();
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            try {
                IOUtils.copy(in, out);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
