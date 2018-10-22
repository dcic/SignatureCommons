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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SigCSignatureLibrary {
    static public List<Object> signatureByCategory(NamedParameterJdbcTemplate jdbcTemplate, String assay_category){
        return signatureByCategory(jdbcTemplate.getJdbcTemplate(),assay_category);
    }
    static public List<Object> signatureByCategory(JdbcTemplate jdbcTemplate, String assay_category){
        List<Object> signatures= new ArrayList<>();
        String sql = "select * from signature where assay_category = ?";
        List<Object> args= new ArrayList<>();
        args.add(assay_category);
        //if no type is passed then get all
        // otherwise add type to the query
        signatures = jdbcTemplate.query(sql,
                args.toArray(),
                new RowMapper<Object>() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String,Object> map = new HashMap<>();
                        map.put("signature_id",rs.getInt(1));
                        map.put("assay_category",rs.getString(2));
                        map.put("data_level",rs.getString(3));
                        map.put("dataset_id",rs.getString(4));
                        return map;
                    }
                }
        );
        return signatures;
    }

    static public Long signatureCountByCategory(NamedParameterJdbcTemplate jdbcTemplate, String assay_category){
        return signatureCountByCategory(jdbcTemplate.getJdbcTemplate(),assay_category);
    }

    static public Long signatureCountByCategory(JdbcTemplate jdbcTemplate, String assay_category){
        String sql = "select count(*) from signature where assay_category = ?";
        List<Object> args= new ArrayList<>();
        args.add(assay_category);
        //if no type is passed then get all
        // otherwise add type to the query
        Long count = jdbcTemplate.queryForObject(sql,args.toArray(),Long.class);
        return count;
    }

    static public List<Object> signatureByDatasetID(NamedParameterJdbcTemplate jdbcTemplate, List<String> dataset_id){
        List<Object> signatures= new ArrayList<>();
        String sql = "select * from signature where dataset_id IN(?)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",dataset_id);
        //if no type is passed then get all
        // otherwise add type to the query
        signatures = jdbcTemplate.query(sql,
                parameters,
                new RowMapper<Object>() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String,Object> map = new HashMap<>();
                        map.put("signature_id",rs.getInt(1));
                        map.put("assay_category",rs.getString(2));
                        map.put("data_level",rs.getString(3));
                        map.put("dataset_id",rs.getString(4));
                        return map;
                    }
                }
        );
        return signatures;
    }
}
