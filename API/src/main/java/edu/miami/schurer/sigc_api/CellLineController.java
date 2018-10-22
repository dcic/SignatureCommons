package edu.miami.schurer.sigc_api;

import edu.miami.schurer.sigc_api.library.SigCCellLineLibrary;
import edu.miami.schurer.sigc_api.library.SigCSmallMoleculeLibrary;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/cell-line")
public class CellLineController extends BaseController{

    @RequestMapping(path="/fetch-by-id", method= RequestMethod.GET)
    public Object fetchByID(@ApiParam(value = "List of Cell line IDs") @RequestParam(value="id",defaultValue = "0") List<Integer> id) {

        // if term is less than 2 then return a blank result
        // otherwise get all the terms that match the term
        if(id.size()==0){
            return formatResults(new ArrayList<>());
        }else {
            return formatResults(SigCCellLineLibrary.fetchCellLine(JDBCTemplate,id));
        }
    }
}
