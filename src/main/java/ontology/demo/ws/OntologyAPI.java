package ontologie.demo.ws;

import net.minidev.json.JSONObject;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class OntologyAPI {

    private final static String FILE_NAME = "gym_semantic.owl";
    private final static String URL = "http://www.semanticweb.org/opendev/ontologies/2017/10/untitled-ontology-8#";

    private OntModel getOntModel() throws FileNotFoundException {
        File file = new File(FILE_NAME);
        FileReader reader = new FileReader(file);
        OntModel model = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(reader,null);
        return model;
    }

    private List<JSONObject> getJSONList(Iterator iterator) {
        List<JSONObject> list=new ArrayList<>();
        while (iterator.hasNext()) {
            OntClass sub = (OntClass) iterator.next();
            JSONObject obj = new JSONObject();
            obj.put("URI",sub.getURI());
            list.add(obj);
        }
        return list;
    }

    private List<JSONObject> getJsonObjects(List<JSONObject> list, Iterator subIter) {
        while (subIter.hasNext()) {
            Individual sub = (Individual) subIter.next();
            JSONObject obj = new JSONObject();
            obj.put("name",sub.getLocalName());
            obj.put("uri",sub.getURI());
            list.add(obj);

        }

        return list;
    }

    @RequestMapping(value = "/ontologies",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getOntologies() {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            Iterator ontologiesIterator = model.listOntologies();
            while (ontologiesIterator.hasNext()) {
                Ontology ontology = (Ontology) ontologiesIterator.next();

                JSONObject obj = new JSONObject();
                obj.put("name",ontology.getLocalName());
                obj.put("uri",ontology.getURI());
                list.add(obj);

            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/classesList",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getClasses() {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            Iterator classIter = model.listClasses();
            while (classIter.hasNext()) {
                OntClass ontClass = (OntClass) classIter.next();
                JSONObject obj = new JSONObject();
                obj.put("name",ontClass.getLocalName());
                obj.put("uri",ontClass.getURI());
                list.add(obj);

            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/subClasses",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getSubClasses(@RequestParam("classname") String className) {
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);
            OntClass received = model.getOntClass(classURI);
            Iterator iterator = received.listSubClasses();
            return getJSONList(iterator);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/Individuals",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getIndividuals() {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            Iterator individuals = model.listIndividuals();
            return getJsonObjects(list, individuals);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/superClasses",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getSuperClasses(@RequestParam("classname") String className) {
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);
            OntClass received = model.getOntClass(classURI);
            Iterator iterator = received.listSuperClasses();
            return getJSONList(iterator);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getClassProperty",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getClassProperty(@RequestParam("classname") String className) {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);

            OntClass ontClass = model.getOntClass(classURI );
            Iterator subIter = ontClass.listDeclaredProperties();
            while (subIter.hasNext()) {
                OntProperty property = (OntProperty) subIter.next();
                JSONObject obj = new JSONObject();
                obj.put("propertyName",property.getLocalName());

                    obj.put("propertyType",property.getRDFType().getLocalName());

                if(property.getDomain()!=null)
                    obj.put("domain", property.getDomain().getLocalName());
                if(property.getRange()!=null)
                    obj.put("range",property.getRange().getLocalName());

                list.add(obj);
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/equivClasses",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getequivClasses(@RequestParam("classname") String className) {
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);
            OntClass received = model.getOntClass(classURI);
            Iterator iterator = received.listEquivalentClasses();

            return getJSONList(iterator);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/Instances",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> getInstancesClasses(@RequestParam("classname") String className) {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);
            OntClass received = model.getOntClass(classURI);
            Iterator subIter = received.listInstances();
            return getJsonObjects(list, subIter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(value = "/isHierarchyRoot",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> isHieararchyRoot(@RequestParam("classname") String className) {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();
            String classURI = URL.concat(className);
            OntClass received = model.getOntClass(classURI );

            if (received != null){
                JSONObject obj = new JSONObject();
                if (received.isHierarchyRoot()){
                    obj.put("isRoot","true");
                }else {
                    obj.put("isRoot","false");
                }
                list.add(obj);
            }
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/query",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JSONObject> query() {
        List<JSONObject> list=new ArrayList<>();
        try {
            OntModel model = getOntModel();

            String sparql = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                    "select * {?x ?y ?z}";
            Query query = QueryFactory.create(sparql);
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            ResultSet resultSet = qe.execSelect();
            while (resultSet.hasNext()) {
                JSONObject obj = new JSONObject();
                QuerySolution solution = resultSet.nextSolution();
                System.out.println(solution.get("x").toString());
                obj.put("subject",solution.get("x").toString());
                obj.put("property",solution.get("y").toString());
                obj.put("object",solution.get("z").toString());
                list.add(obj);
            }
            return list;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
