package edu.miami.schurer.sigc_api.library;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigCSmallMoleculeLibrary {

    /**
     * Used a list of Perturbagen IDs to get all matching small molecules as well as their matching signature ids
     * @param jdbcTemplate
     * @param perturbagen_id
     * @return List<SmallMolecule> A list of small molecules from the small_molecule table
     */
    static public List<Map<String,Object>> fetchSmallMolecule(NamedParameterJdbcTemplate jdbcTemplate, List<Integer> perturbagen_id){
        List<Map<String,Object>> SmallMolecules= new ArrayList<>();
        String sql = "select * from lookup_signature_object_perturbation_small_molecule where perturbagen_id = ? ";
        final Integer[] ids = perturbagen_id.toArray(new Integer[0]);
        PreparedStatementSetter parameters = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement stmt) throws SQLException {
                Connection conn = stmt.getConnection();
                // this can only be done through the Connection
                java.sql.Array arr = conn.createArrayOf("integer", ids);
                // you can use setObject(1, ids, java.sql.Types.ARRAY) instead of setArray
                // in case the connection wrapper doesn't pass it on to the JDBC driver
                stmt.setArray(1, arr);
            }
        };

        JdbcOperations jdo = jdbcTemplate.getJdbcOperations();
        //if no type is passed then get all
        // otherwise add type to the query
        SmallMolecules = jdo.query(sql,
                parameters,
                new RowMapper<Map<String,Object>>() {
                    // map to hold counts of assay categories
                    Map<String,Integer> categories = new HashMap<>();
                    //TODO Move this query outside of this function so its only called once and then combine below
                    //Attempted, More difficult than assumed
                    public Map<String,Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String,Object> smallMolecule = new HashMap<>();
                        String signatureSql = "select sm.signature_id,s.assay_category from lookup_signature_object_perturbation_small_molecule sm " +
                                "INNER JOIN signature s on s.signature_id = sm.signature_id " +
                                "where perturbagen_id && ? and perturbagen_class = ARRAY[?]::VARCHAR[]";
                        List<Map<String,Object>> signature = jdbcTemplate.getJdbcTemplate().query(signatureSql,
                                new Object[]{rs.getArray("perturbagen_id"),"small molecule"},
                                new RowMapper<Map<String,Object>>() {
                                    public Map<String,Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                                        Map<String,Object> r = new HashMap<>();
                                        r.put("signature_id",rs.getString("signature_id"));
                                        r.put("assay_category",rs.getString("assay_category"));
                                        // count categories
                                        if(!categories.containsKey(rs.getString("assay_category")))
                                            categories.put(rs.getString("assay_category"),0);
                                        categories.put(rs.getString("assay_category"),categories.get(rs.getString("assay_category"))+1);
                                        return  r;
                                    }
                                }
                        );
                        smallMolecule.put("perturbagen_id",rs.getArray("perturbagen_id").getArray());
                        smallMolecule.put("sm_name",rs.getArray("small_molecule_name").getArray());
                        smallMolecule.put("canonical_smiles",rs.getArray("canonical_smiles").getArray());
                        smallMolecule.put("canonical_inchi_key",rs.getArray("canonical_inchi_key").getArray());
                        smallMolecule.put("canonical_inchi",rs.getArray("canonical_inchi").getArray());
                        smallMolecule.put("max_fda_phase",rs.getArray("max_fda_phase").getArray());
                        smallMolecule.put("signature",signature);
                        smallMolecule.put("signature_count",signature.size());
                        smallMolecule.put("signature_category_count",categories);
                        return smallMolecule;
                    }
                }
        );
        return SmallMolecules;
    }

    static public List<Object> fetchSmallMoleculeProperties(NamedParameterJdbcTemplate jdbcTemplate, List<Integer> perturbagen_id){
        List<Object> SmallMolecules= new ArrayList<>();
        String sql = "SELECT perturbagen_id, json_object_agg(property_symbol, property_value) as return FROM small_molecule_physical_properties  where perturbagen_id IN (:ids) GROUP BY perturbagen_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",perturbagen_id);

        //if no type is passed then get all
        // otherwise add type to the query
        SmallMolecules = jdbcTemplate.query(sql,
                parameters,
                new RowMapper<Object>() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ObjectMapper mapper = new ObjectMapper();

                        Map<String, Object> map = new HashMap<String, Object>();

                        // convert JSON string to Map
                        try {
                            map = mapper.readValue(rs.getString(2), new TypeReference<Map<String, String>>() {});
                        } catch (JsonGenerationException e) {
                            e.printStackTrace();
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        map.put("perturbagen_id",rs.getInt(1));
                        return map;
                    }
                }
        );
        return SmallMolecules;
    }
    static public List<Map<String, Object>> fetchMechanismOfAction(NamedParameterJdbcTemplate jdbcTemplate, Integer id){
        return fetchMechanismOfAction(jdbcTemplate.getJdbcTemplate(),id);
    }
    static public List<Map<String, Object>> fetchMechanismOfAction(JdbcTemplate jdbcTemplate, Integer id) {
        List<Map<String, Object>> signatures = new ArrayList<>();
        String sql = "SELECT perturbagen_id, mechanism_of_action.mechanism_of_action, target_name, target_gene_symbol, target_type,action_type " +
                "FROM mechanism_of_action " +
                "WHERE perturbagen_id = ?" +
                "AND perturbagen_class = 'small molecule'";
        List<Object> args = new ArrayList<>();
        args.add(id);
        //if no type is passed then get all
        // otherwise add type to the query
        signatures = jdbcTemplate.query(sql,
                args.toArray(),
                new RowMapper<Map<String, Object>>() {
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("perturbagen_id", rs.getInt(1));
                        map.put("mechanism_of_action", rs.getString(2));
                        map.put("target_name", rs.getString(3));
                        map.put("target_gene_symbol", rs.getString(4));
                        map.put("target_type", rs.getString(5));
                        map.put("action_type", rs.getString(6));
                        return map;
                    }
                }
        );
        return signatures;
    }

    static public List<Map<String, Object>> fetchBioactivtyTargets(NamedParameterJdbcTemplate jdbcTemplate, Integer id){
        return fetchBioactivtyTargets(jdbcTemplate.getJdbcTemplate(),id);
    }
    static public List<Map<String, Object>> fetchBioactivtyTargets(JdbcTemplate jdbcTemplate, Integer id) {
        List<Map<String, Object>> signatures = new ArrayList<>();
        String sql = "SELECT perturbagen_id, mechanism_of_action.mechanism_of_action, target_name, target_gene_symbol, target_type,action_type " +
                "FROM mechanism_of_action " +
                "WHERE perturbagen_id = ?" +
                "AND perturbagen_class = 'small molecule'";
        List<Object> args = new ArrayList<>();
        args.add(id);
        //if no type is passed then get all
        // otherwise add type to the query
        signatures = jdbcTemplate.query(sql,
                args.toArray(),
                new RowMapper<Map<String, Object>>() {
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("perturbagen_id", rs.getInt(1));
                        map.put("mechanism_of_action", rs.getString(2));
                        map.put("target_name", rs.getString(3));
                        map.put("target_gene_symbol", rs.getString(4));
                        map.put("target_type", rs.getString(5));
                        map.put("action_type", rs.getString(6));
                        return map;
                    }
                }
        );
        return signatures;
    }
}
