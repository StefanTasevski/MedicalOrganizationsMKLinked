package ontology.demo.ws;

import net.minidev.json.JSONObject;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class OntologyAPI {

    /*
     * Note: The properties not defined by us need to be added manually.
     * */

    private final static String FILE_NAME = "dataset.owl";

    @GetMapping(value = "/ontology")
    public List<JSONObject> getOntologies() {
        return getList(getModel().listOntologies(), true);
    }

    @GetMapping(value = "/type")
    public List<JSONObject> getTypes() {
        return getList(getModel().listClasses(), true);
    }

    @GetMapping(value = "/type/{name}")
    public List<JSONObject> getTypeProperties(@PathVariable("name") String name) {
        return getPropertiesList(findClass(name).listDeclaredProperties());
    }

    @GetMapping(value = "/type/{name}/subType")
    public List<JSONObject> getSubTypes(@PathVariable("name") String name) {
        OntClass superClass = findClass(name);
        assert superClass != null;
        return getList(superClass.listSubClasses(), true);
    }

    @GetMapping(value = "/type/{name}/superType")
    public List<JSONObject> getSuperTypes(@PathVariable("name") String name) {
        OntClass subClass = findClass(name);
        assert subClass != null;
        return getList(subClass.listSuperClasses(), true);
    }

    @GetMapping(value = "/instance")
    public List<JSONObject> getInstances() {
        return getList(getModel().listIndividuals(), false);
    }

    @GetMapping(value = "/instance/{name}")
    public List<JSONObject> getTypeInstances(@PathVariable("name") String className) {
        return getList(findClass(className).listInstances(), false);
    }

    /**
     * Example body:
     * prefix md: <http://www.medicalorganizations.mk#>
     * select ?p ?o {
     *    md:1376900939 ?p ?o
     * }
     **/
    @PostMapping(value = "/sparql")
    public List<JSONObject> executeQuery(@RequestBody String query) {

        List<JSONObject> list = new ArrayList<>();
        QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(query), getModel());
        ResultSet resultSet = qe.execSelect();

        while (resultSet.hasNext()) {
            JSONObject obj = new JSONObject();
            QuerySolution solution = resultSet.nextSolution();
            solution.varNames().forEachRemaining(var ->
                    obj.put(var, solution.get(var).toString())
            );
            list.add(obj);
        }
        return list;
    }

    private OntModel getModel() {
        FileReader reader = null;
        try {
            reader = new FileReader(new File(FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(reader, null);
        return model;
    }

    private List<JSONObject> getList(Iterator iterator, Boolean hasName) {
        List<JSONObject> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Resource sub = (Resource) iterator.next();
            JSONObject obj = new JSONObject();
            if (hasName) {
                obj.put("name", sub.getLocalName());
            }
            obj.put("uri", sub.getURI());
            list.add(obj);
        }
        return list;
    }

    private List<JSONObject> getPropertiesList(Iterator iterator) {
        List<JSONObject> list = new ArrayList<>();
        while (iterator.hasNext()) {
            OntProperty property = (OntProperty) iterator.next();
            JSONObject obj = new JSONObject();
            obj.put("name", property.getLocalName());
            obj.put("type", property.getRDFType().getLocalName());
            if (property.getDomain() != null)
                obj.put("domain", property.getDomain().getLocalName());
            if (property.getRange() != null)
                obj.put("range", property.getRange().getLocalName());
            list.add(obj);
        }
        return list;
    }

    private OntClass findClass(String className) {
        return getModel().listClasses().toList().stream()
                .filter(c -> c.getLocalName().equals(className))
                .findFirst()
                .orElse(null);
    }
}
