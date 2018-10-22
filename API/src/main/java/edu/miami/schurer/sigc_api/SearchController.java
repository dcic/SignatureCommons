package edu.miami.schurer.sigc_api;

import java.util.*;

import edu.miami.schurer.sigc_api.resources.Suggests;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import edu.miami.schurer.sigc_api.library.*;


@RestController
@RequestMapping("/search")
@Api(description = "Set of endpoints for searching the relational database for partcular IDs")
public class SearchController extends BaseController{

    @RequestMapping(path="/suggest", method=RequestMethod.GET)
    @ApiOperation("Returns an object containing count of objects returned and list of database objects matching term")
    public Object suggests(@RequestParam(value="term",defaultValue = "") String term,@RequestParam(value="UI",required = false) boolean UI) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();

        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(results,new ArrayList<>());
        }else {

            //remove sensitive results
            List<Suggests> suggests;
            if(UI)
                suggests= SigCSearchLibrary.suggestsUI(JDBCTemplate, term.toLowerCase()); //TODO: confirm we want everything to be lowercased
            else{
                suggests= SigCSearchLibrary.suggests(JDBCTemplate, term.toLowerCase());
            }
            for (Suggests s:suggests) {
                String hitType = s.getHit_type();
                List<Object> identifiersList = new ArrayList<>();
                if(results.containsKey(hitType)){
                    identifiersList = (List<Object>)results.get(hitType);
                }else{
                    results.put(hitType,identifiersList);
                }
                HashMap<String,Object> suggest = new HashMap<>();
                suggest.put("suggest_term",s.getSuggest_term());
                suggest.put("preferred_term",s.getPreferred_term());
                if(!UI)
                    suggest.put("hit_object_id",s.getHit_object_id());
                suggest.put("hit_object_class",s.getHit_object_class());
                suggest.put("hit_type",s.getHit_type());
                identifiersList.add(suggest);
            }

            return formatResults(results);
        }
    }
    @RequestMapping(path="/get-facets", method=RequestMethod.GET)
    public Object suggests(@RequestParam(value="term",defaultValue = "") String term,@RequestParam(value="type",defaultValue = "") String type) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String,HashMap<String,Object>> results = new HashMap<>();

        //list that prevents double counting
        HashMap<String,List<Integer>> nameMappingList = new HashMap<>();

        results.put("class_count", new HashMap<>());
        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(new ArrayList<>());
        }else {
            //get all the suggests hit IDs that match the current terms
            List<Suggests> suggests= SigCSearchLibrary.suggests(JDBCTemplate, term.toLowerCase(),"",type,false,false);
/*            List<Integer> hitIDS = new ArrayList<>();
            for (Suggests s:suggests) {
                hitIDS.add(s.getHit_object_id());
            }


            //get the counts for these
            suggests= SigCSearchLibrary.suggestsByID(JDBCTemplate, hitIDS);*/
            for (Suggests s:suggests) {
                //Check if we have a count for the current class, if not add it
                if(!results.containsKey(s.getHit_object_class())){
                    results.put(s.getHit_object_class(), new HashMap<>());
                    results.get("class_count").put(s.getHit_object_class(),new ArrayList<Integer>());
                }
                HashMap<String,Object> classMap = results.get(s.getHit_object_class());


                //Check if we have a count for the current type, if not add it
                if(!classMap.containsKey(s.getHit_type())){
                    classMap.put(s.getHit_type(),new HashMap<>());
                }
                HashMap<String, Integer> typeCount = (HashMap<String, Integer>)classMap.get(s.getHit_type());


                //Check if we have a count for the current term, if not add it
                if(!typeCount.containsKey(s.getPreferred_term())){
                    typeCount.put(s.getPreferred_term(),0);
                }

                // add entry to nameMappingList if it doesn't exists
                if(!nameMappingList.containsKey(s.getHit_object_class()+" "+s.getHit_type()+" "+s.getPreferred_term())){
                    nameMappingList.put(s.getHit_object_class()+" "+s.getHit_type()+" "+s.getPreferred_term(), new ArrayList<>());
                }

                // if id is not in mapping list then add it and count it
                if(!nameMappingList.get(s.getHit_object_class()+" "+s.getHit_type()+" "+s.getPreferred_term()).contains(s.getHit_object_id())) {
                    typeCount.put(s.getPreferred_term(), typeCount.get(s.getPreferred_term()) + 1);
                    nameMappingList.get(s.getHit_object_class()+" "+s.getHit_type()+" "+s.getPreferred_term()).add(s.getHit_object_id());
                }

                //add id to class list
                List<Integer> classCount = (List<Integer>)results.get("class_count").get(s.getHit_object_class());
                if(!classCount.contains(s.getHit_object_id())) {
                    classCount.add(s.getHit_object_id());
                }

            }
            for(String s: results.get("class_count").keySet()){
                List<Integer> classCount =  (List<Integer>)results.get("class_count").get(s);
                results.get("class_count").put(s,classCount.size());

            }

/*            //sort results
            for(String s: results.keySet()){
                if(!s.equals("class_count")){
                    for(String k: results.get(s).keySet()){
                        Collections.sort((List<Intresults.get(s).get(k));
                    }
                }

            }*/


            return formatResults(results);
        }
    }

    @RequestMapping(path="/autosuggest", method=RequestMethod.GET)
    public Object autosuggests(@RequestParam(value="term",defaultValue = "") String term) {



        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(new ArrayList<>());
        }else {

            //remove sensitive results
            List<Suggests> suggests= SigCSearchLibrary.distinctSuggests(JDBCTemplate, term,"","",true);
            List<Object> suggestsReturn = new ArrayList<>();
            for (Suggests s:suggests) {
                HashMap<String,Object> result = new HashMap<>();
                result.put("suggest_term",s.getSuggest_term());
                result.put("preferred_term",s.getPreferred_term());
                result.put("hit_object_id",s.getHit_object_id());
                result.put("hit_object_class",s.getHit_object_class());
                result.put("hit_type",s.getHit_type());
                suggestsReturn.add(result);
            }


            return formatResults(suggestsReturn);
        }
    }
/*
    @RequestMapping(path="/perturbagen", method=RequestMethod.GET)
    public Object perturbagens(@RequestParam(value="term",defaultValue = "") List<String> term,@RequestParam(value="type",defaultValue = "") List<String> type) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();

        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(results,new ArrayList<>());
        }else {

            //remove sensitive results
            List<Suggests> suggests= SigCSearchLibrary.suggests(JDBCTemplate, term);
            List<Object> suggestsReturn = new ArrayList<>();
            for (Suggests s:suggests) {
                HashMap<String,Object> result = new HashMap<>();
                result.put("suggest_term",s.getSuggest_term());
                result.put("preferred_term",s.getPreferred_term());
                result.put("hit_object_id",s.getHit_object_id());
                result.put("hit_object_class",s.getHit_object_class());
                result.put("hit_type",s.getHit_type());
                suggestsReturn.add(result);
            }


            return formatResults(results,suggestsReturn);
        }
    }
    @RequestMapping(path="/model-system", method=RequestMethod.GET)
    public Object perturbagens(@RequestParam(value="term",defaultValue = "") List<String> term,@RequestParam(value="type",defaultValue = "") List<String> type) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();

        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(results,new ArrayList<>());
        }else {

            //remove sensitive results
            List<Suggests> suggests= SigCSearchLibrary.suggests(JDBCTemplate, term);
            List<Object> suggestsReturn = new ArrayList<>();
            for (Suggests s:suggests) {
                HashMap<String,Object> result = new HashMap<>();
                result.put("suggest_term",s.getSuggest_term());
                result.put("preferred_term",s.getPreferred_term());
                result.put("hit_object_id",s.getHit_object_id());
                result.put("hit_object_class",s.getHit_object_class());
                result.put("hit_type",s.getHit_type());
                suggestsReturn.add(result);
            }


            return formatResults(results,suggestsReturn);
        }
    }
    @RequestMapping(path="/signatures", method=RequestMethod.GET)
    public Object perturbagens(@RequestParam(value="term",defaultValue = "") List<String> term,@RequestParam(value="type",defaultValue = "") List<String> type) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();

        // if term is less than 2 then return a blank results
        // otherwise get all the terms that match the term
        if(term.length() < 2){
            return formatResults(results,new ArrayList<>());
        }else {

            //remove sensitive results
            List<Suggests> suggests= SigCSearchLibrary.suggests(JDBCTemplate, term);
            List<Object> suggestsReturn = new ArrayList<>();
            for (Suggests s:suggests) {
                HashMap<String,Object> result = new HashMap<>();
                result.put("suggest_term",s.getSuggest_term());
                result.put("preferred_term",s.getPreferred_term());
                result.put("hit_object_id",s.getHit_object_id());
                result.put("hit_object_class",s.getHit_object_class());
                result.put("hit_type",s.getHit_type());
                suggestsReturn.add(result);
            }


            return formatResults(results,suggestsReturn);
        }
    }

*/
    @RequestMapping(path="/count", method=RequestMethod.GET)
    public Object count(@RequestParam(value="term",defaultValue = "") String term) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();


        //Build the objects needed for counts
        HashMap<String, Integer> perturbagen = new HashMap<>();
            //get the counts for all pertubagens
            perturbagen.put("small_molecules",SigCSearchLibrary.suggests(JDBCTemplate, "","small molecule","name",true,true).size());
            perturbagen.put("shRNAs",SigCSearchLibrary.suggests(JDBCTemplate, "","shRNA","gene target",true,true).size());
            perturbagen.put("sgRNAs",SigCSearchLibrary.suggests(JDBCTemplate, "","sgRNA","gene target",true,true).size());

            //add the counts of the perturbagens since there is not an easy query to get them all
            perturbagen.put("count",perturbagen.get("small_molecules")+perturbagen.get("shRNAs") +perturbagen.get("sgRNAs"));
        results.put("perturbagens",perturbagen);

        HashMap<String, Integer> cells = new HashMap<>();
            // get the cell counts
            cells.put("cells",SigCSearchLibrary.suggests(JDBCTemplate, "","cell line","name",true,true).size());
            cells.put("cell_types",1); //TODO Check where to get this
            cells.put("diseases",SigCSearchLibrary.suggests(JDBCTemplate, "","cell line","disease",true,true).size());
            cells.put("tissue_types",SigCSearchLibrary.suggests(JDBCTemplate, "","cell line","tissue",true,true).size());
            cells.put("count",SigCSearchLibrary.suggests(JDBCTemplate, "","cell line","",true,true).size());
        results.put("cells",cells);
        HashMap<String, Long> signatures = new HashMap<>();
            // get the signature counts
            signatures.put("gene_expression",SigCSignatureLibrary.signatureCountByCategory(JDBCTemplate,"gene expression"));
            signatures.put("proteomics",SigCSignatureLibrary.signatureCountByCategory(JDBCTemplate,"proteomics"));
            signatures.put("epigenetic",SigCSignatureLibrary.signatureCountByCategory(JDBCTemplate,"epigenetic"));
            signatures.put("count",signatures.get("gene_expression")+signatures.get("proteomics") +signatures.get("epigenetic"));
        results.put("signatures",signatures);

        return results;
    }
    @RequestMapping(path="/identifiers", method=RequestMethod.GET)
    public Object identifiers(@RequestParam(value="id",defaultValue = "") List<Integer> id,@RequestParam(value="object_class",defaultValue = "") List<String> object_class) {

        //create a key-value dictionary from a hashmap that has a string key and any possible value
        HashMap<String, Object> results = new HashMap<>();
        List<Object> aggregated = new ArrayList<>();
        for (int i = 0; i< id.size(); i++){
            //map to hold this identifiers information
            HashMap<String, Object> identifierMap = new HashMap<>();
            //query for this set of identifiers
            List<Map<String, Object>> identifiers = SigCSearchLibrary.fetchIdentifiers(JDBCTemplate,id.get(i),object_class.get(i));
            identifierMap.put("object_id",id.get(i));
            for (Map<String, Object> o:identifiers) {
                String sourceName = (String)o.get("source_name");
                List<String> identifiersList = new ArrayList<>();
                if(identifierMap.containsKey(sourceName)){
                    identifiersList = (List<String>)identifierMap.get(sourceName);
                }else{
                    identifierMap.put(sourceName,identifiersList);
                }
                identifiersList.add((String)o.get("external_id"));
            }
            aggregated.add(identifierMap);
        }
        return formatResults(results,aggregated);
    }
}
