package edu.miami.schurer.sigc_api;

import edu.miami.schurer.sigc_api.library.SigCSearchLibrary;
import edu.miami.schurer.sigc_api.library.SigCSmallMoleculeLibrary;
import edu.miami.schurer.sigc_api.resources.Suggests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/small-molecule")
public class SmallMoleculeController extends BaseController {


    @RequestMapping(path="/fetch-by-id", method= RequestMethod.GET)
    public Object fetchByID(@RequestParam(value="id",defaultValue = "0") List<Integer> id) {

        // if term is less than 2 then return a blank result
        // otherwise get all the terms that match the term
        if(id.size()==0){
            return formatResults(new ArrayList<>());
        }else {
            return formatResults(SigCSmallMoleculeLibrary.fetchSmallMolecule(JDBCTemplate,id));
        }
    }

    @RequestMapping(path="/fetch-by-name", method= RequestMethod.GET)
    public Object fetchByName(@RequestParam(value="name",defaultValue = "") String name) {

        // if term is less than 2 then return a blank result
        // otherwise get all the terms that match the term
        if(name.length()<2){
            return formatResults(new ArrayList<>());
        }else {
            List<Integer> ids = new ArrayList<>();
            List<Suggests> matchingHits = SigCSearchLibrary.suggests(JDBCTemplate,name,"small molecule");
            for (Suggests s:matchingHits) {
                ids.add(s.getHit_object_id());
            }
            return  formatResults(SigCSmallMoleculeLibrary.fetchSmallMolecule(JDBCTemplate,ids));
        }
    }

    @RequestMapping(path="/fetch-properties", method= RequestMethod.GET)
    public Object fetchByName(@RequestParam(value="id",defaultValue = "0") List<Integer> id) {

        // if term is less than 2 then return a blank result
        // otherwise get all the terms that match the term
        if(id.size()==0){
            return  formatResults(new ArrayList<>());
        }else {
            return formatResults(SigCSmallMoleculeLibrary.fetchSmallMoleculeProperties(JDBCTemplate,id));
        }
    }
    @RequestMapping(path="/fetch-identifiers", method= RequestMethod.GET)
    public Object fetchByIdentifiers(@RequestParam(value="id",defaultValue = "0") List<Integer> id) {

        // if term is less than 2 then return a blank result
        // otherwise get all the terms that match the term
        if(id.size()==0){
            return formatResults(new ArrayList<>());
        }else {
            return formatResults(SigCSmallMoleculeLibrary.fetchSmallMoleculeProperties(JDBCTemplate,id));
        }
    }

    @RequestMapping(path="/fetch-mechanism-of-action", method= RequestMethod.GET)
    public Object fetchByMechanismOfAction(@RequestParam(value="id",defaultValue = "0") List<Integer> id) {

        List<Object> aggregated = new ArrayList<>();
        for (int i = 0; i< id.size(); i++){
            //map to hold this identifiers information
            HashMap<String, Object> MOAMap = new HashMap<>();
            //query for this set of mechanisms of action
            List<Map<String, Object>> identifiers = SigCSmallMoleculeLibrary.fetchMechanismOfAction(JDBCTemplate,id.get(i));

            //set the id for the set
            MOAMap.put("perturbagen_id",id.get(i));

            //remove the perturbagen_id from the array
            for(Map<String, Object> o: identifiers){
                o.remove("perturbagen_id");
            }
            MOAMap.put("mechanism_of_action",identifiers);
            MOAMap.put("count",identifiers.size());
            //add the aggregated results to the results
            aggregated.add(MOAMap);
        }
        return formatResults(aggregated);
    }
}
