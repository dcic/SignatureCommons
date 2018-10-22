package edu.miami.schurer.sigc_api.library;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.miami.schurer.sigc_api.resources.Suggests;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigCSearchLibrary {
    static public List<Suggests> suggests(JdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate,term,"","",false,false,false);
    }

    static public List<Suggests> suggests(JdbcTemplate jdbcTemplate, String term,String object_type){
        return suggests(jdbcTemplate,term,object_type,"",false,false,false);
    }
    static public List<Suggests> suggests(NamedParameterJdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,"","",false,false,false);
    }
    static public List<Suggests> suggests(NamedParameterJdbcTemplate jdbcTemplate, String term,String object_type){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,object_type,"",false,false,false);
    }
    static public List<Suggests> suggests(NamedParameterJdbcTemplate jdbcTemplate, String term,String object_type,String hit_type,boolean distinct,boolean withoutSuggestTerm){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,object_type,hit_type,distinct,withoutSuggestTerm,false);
    }


    static public List<Suggests> distinctSuggests(NamedParameterJdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,"","",true,false,false);
    }
    static public List<Suggests> distinctSuggests(NamedParameterJdbcTemplate jdbcTemplate, String term,String object_type){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,object_type,"",true,false,false);
    }
    static public List<Suggests> distinctSuggests(NamedParameterJdbcTemplate jdbcTemplate, String term,String object_type,String hit_type){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,object_type,hit_type,true,false,false);
    }
    static public List<Suggests> distinctSuggests(NamedParameterJdbcTemplate jdbcTemplate, String term,String object_type,String hit_type,boolean withoutSuggestTerm){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,object_type,hit_type,true,withoutSuggestTerm,false);
    }
    static public List<Suggests> distinctSuggests(JdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate,term,"","",true,false,false);
    }
    static public List<Suggests> distinctSuggests(JdbcTemplate jdbcTemplate, String term,String object_type){
        return suggests(jdbcTemplate,term,object_type,"",true,false,false);
    }
    static public List<Suggests> distinctSuggests(JdbcTemplate jdbcTemplate, String term,String object_type,String hit_type){
        return suggests(jdbcTemplate,term,object_type,hit_type,true,false,false);
    }
    static public List<Suggests> distinctSuggests(JdbcTemplate jdbcTemplate, String term,String object_type,String hit_type,boolean withoutSuggestTerm){
        return suggests(jdbcTemplate,term,object_type,hit_type,true,withoutSuggestTerm,false);
    }


    static public List<Suggests> suggestsUI(JdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate,term,"","",true,false,true);
    }
    static public List<Suggests> suggestsUI(NamedParameterJdbcTemplate jdbcTemplate, String term){
        return suggests(jdbcTemplate.getJdbcTemplate(),term,"","",true,false,true);
    }

    static public List<Suggests> suggests(JdbcTemplate jdbcTemplate, String term,String object_type,String hit_type,boolean distinct,boolean withoutSuggestTerm,boolean withoutObjectID){

        String sql = "select ";

        //if we want a distinct lookup
        if(distinct)
            sql+="distinct ";

        if(!withoutSuggestTerm)
            sql +=  "suggest_term, ";
        if(!withoutObjectID)
            sql +=  "hit_object_id, ";
        sql +=  "hit_object_lookup_table, " +
                "hit_object_lookup_table_id_field, " +
                "hit_object_lookup_table_class_field, " +
                "hit_object_class, " +
                "suggest_object_preferred_name, " +
                "hit_type " +
                "from autosuggest WHERE (lower(suggest_term) like ? or lower(suggest_term) like ?)" +
                "and hit_object_lookup_table IS NOT NULL";

        //if no type is passed then get all
        // otherwise add type to the query
        List<Object> args = new ArrayList<>();
        args.add(term + "%");
        args.add("% " + term + "%");

        //if we have a object type add it here
        if(!object_type.isEmpty()) {
            sql+=" AND hit_object_class = ?";
            args.add(object_type);
        }

        //if we have a hit type add it here
        if(!hit_type.isEmpty()) {
            sql+=" AND hit_type = ?";
            args.add(hit_type);
        }

        return jdbcTemplate.query(sql,
                args.toArray(),
                new RowMapper<Suggests>() {
                    public Suggests mapRow(ResultSet rs, int rowNum) throws SQLException {

                        //we don't always want the search terms, so if we don't requests it we need to not call it
                        String suggests_term = "";
                        if(!withoutSuggestTerm)
                            suggests_term= rs.getString("suggest_term");
                        Integer objectID = 0;
                        if(!withoutObjectID)
                            objectID = rs.getInt("hit_object_id");
                        return new Suggests(
                                suggests_term,
                                objectID,
                                rs.getString("hit_object_lookup_table"),
                                rs.getString("hit_object_lookup_table_id_field"),
                                rs.getString("hit_object_lookup_table_class_field"),
                                rs.getString("hit_object_class"),
                                rs.getString("suggest_object_preferred_name"),
                                rs.getString("hit_type")
                        );
                    }
                }
        );
    }

    static public List<Suggests> suggestsByID(NamedParameterJdbcTemplate jdbcTemplate, List<Integer> hit_object_id) {
        String sql = "select * from autosuggest where hit_object_id IN (:ids) and hit_object_lookup_table IS NOT NULL ";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",hit_object_id);

        return jdbcTemplate.query(sql,
                parameters,
                new RowMapper<Suggests>() {
                    public Suggests mapRow(ResultSet rs, int rowNum) throws SQLException {

                        return new Suggests(
                                rs.getString("suggest_term"),
                                rs.getInt("hit_object_id"),
                                rs.getString("hit_object_lookup_table"),
                                rs.getString("hit_object_lookup_table_id_field"),
                                rs.getString("hit_object_lookup_table_class_field"),
                                rs.getString("hit_object_class"),
                                rs.getString("suggest_object_preferred_name"),
                                rs.getString("hit_type")
                        );
                    }
                }
        );
    }

    static public List<Map<String, Object>> fetchIdentifiers(NamedParameterJdbcTemplate jdbcTemplate, Integer id,String object_class){
        return fetchIdentifiers(jdbcTemplate.getJdbcTemplate(),id,object_class);
    }
    static public List<Map<String, Object>> fetchIdentifiers(JdbcTemplate jdbcTemplate, Integer id,String object_class) {
        List<Map<String, Object>> signatures = new ArrayList<>();
        String sql = "SELECT em.object_id, em.external_id, es.source_name, es.source_description, es.source_url " +
                "FROM public.external_identifier_mapping em " +
                "JOIN external_identifier_sources es ON em.source_id = es.source_id " +
                "WHERE object_id = ?" +
                "AND object_class = ?";
        List<Object> args = new ArrayList<>();
        args.add(id);
        args.add(object_class);
        //if no type is passed then get all
        // otherwise add type to the query
        signatures = jdbcTemplate.query(sql,
                args.toArray(),
                new RowMapper<Map<String, Object>>() {
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("object_id", rs.getInt(1));
                        map.put("external_id", rs.getString(2));
                        map.put("source_name", rs.getString(3));
                        map.put("source_description", rs.getString(4));
                        map.put("source_url", rs.getString(5));
                        return map;
                    }
                }
        );
        return signatures;
    }

}
