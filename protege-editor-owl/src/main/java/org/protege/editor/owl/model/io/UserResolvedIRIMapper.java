package org.protege.editor.owl.model.io;

import org.protege.editor.owl.model.MissingImportHandler;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
/*
* Copyright (C) 2007, University of Manchester
*
*
*/

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 1, 2008<br><br>
 * <p>
 * A custom URIMapper.  This is used by the various parsers to
 * convert ontology URIs into physical URIs that point to concrete
 * representations of ontologies.
 * <p>
 * The mapper uses the following strategy:
 * <p>
 * The system turns to the "Missing Import Handler", which may
 * try to obtain the physical URI (usually by adding a library or
 * by specifying a file etc.)
 */
public class UserResolvedIRIMapper implements OWLOntologyIRIMapper {

    private final Map<IRI, URI> resolvedMissingImports = new HashMap<>();

    private MissingImportHandler missingImportHandler;


    public UserResolvedIRIMapper(MissingImportHandler missingImportHandler) {
        this.missingImportHandler = missingImportHandler;
    }


    public IRI getDocumentIRI(IRI ontologyIRI) {
        if (resolvedMissingImports.containsKey(ontologyIRI)) {
            // Already resolved the missing import - don't ask again
            return IRI.create(resolvedMissingImports.get(ontologyIRI));
        } else {

            URI resolvedURI = resolveMissingImport(ontologyIRI);
            if (resolvedURI != null) {
                resolvedMissingImports.put(ontologyIRI, resolvedURI);
            }
            return resolvedURI != null ? IRI.create(resolvedURI) : null;
        }
    }

    private URI resolveMissingImport(IRI ontologyIRI) {
        // tries to resolve any ROS paths directly without prompting the user.
        IRI rosIri = resolveRosPackageImports(ontologyIRI);
        if (rosIri != null)
            return rosIri.toURI();

        IRI iri = missingImportHandler.getDocumentIRI(ontologyIRI);
        return iri != null ? iri.toURI() : null;
    }

    /**
     * This method will try to get the system path based on a ROS package reference.
     * It will use a system call which will not work on non-Unix systems. But  you shouldn't be opening ROS documents
     * there anyway.
     *
     * @param ontologyIRI the IRI containing the relative path.
     * @return The IRI of the specific file path.
     * If the path is not ROS conform or if something goes wrong the method will return null.
     */
    private IRI resolveRosPackageImports(IRI ontologyIRI) {
        // if it starts with the package prefix then we try to infer it directly, it's probably ros.
        if (ontologyIRI.toString().startsWith("package://")) {
            BufferedReader b = null;
            try {
                //  let's play around with strings till I get what I want.
                String givenUri = ontologyIRI.toString();
                givenUri = givenUri.replaceFirst("package://", "");
                int firstSlash = givenUri.indexOf("/");
                String packageName = givenUri.substring(0, firstSlash);
                String filePath = givenUri.substring(firstSlash);

                // now it is time for a system call.
                Runtime r = Runtime.getRuntime();
                Process p = r.exec("rospack find " + packageName);
                p.waitFor();
                b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String packagePath;
                if ((packagePath = b.readLine()) != null) {
                    File banana = new File(packagePath + filePath);
                    return IRI.create(banana);
                } else return null;
            } catch (Exception e) {
                // I am sad... it didn't work, so let's do it the old way.
                return null;
            } finally {
                if (b != null)
                    try {
                        b.close();
                    } catch (IOException e) {
                        // the system couldn't close the buffered reader. Which shouldn't happen, but I will catch it
                        // anyway.
                    }
            }
        } else
            return null;
    }


    public void setMissingImportHandler(MissingImportHandler handler) {
        missingImportHandler = handler;
    }
}
