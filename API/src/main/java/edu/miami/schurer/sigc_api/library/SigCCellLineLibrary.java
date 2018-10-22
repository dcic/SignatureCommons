package edu.miami.schurer.sigc_api.library;

import edu.miami.schurer.sigc_api.resources.Suggests;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigCCellLineLibrary {
    static public List<Suggests> suggests(JdbcTemplate jdbcTemplate, String term){
        return new ArrayList<>();
    }
    /**
     * Used a list of Model System IDs to get all matching cell lines as well as their matching signature ids
     * @param jdbcTemplate
     * @param model_system_id
     * @return List<SmallMolecule> A list of small molecules from the small_molecule table
     */
    static public List<Map<String,Object>> fetchCellLine(NamedParameterJdbcTemplate jdbcTemplate, List<Integer> model_system_id){
        List<Map<String,Object>> SmallMolecules= new ArrayList<>();
        String sql = "select * from lookup_signature_object_model_system_cell_line where cell_line_id = ANY ( ? )";
        final Integer[] ids = model_system_id.toArray(new Integer[0]);
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
                    Map<String,Integer> categories = new HashMap<>();
                    //TODO Move this query outside of this function so its only called once and then combine below
                    public Map<String,Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String,Object> cellLine = new HashMap<>();
                        String signatureSql = "select sm.signature_id,s.assay_category from lookup_signature_object_model_system_cell_line sm " +
                                "INNER JOIN signature s on s.signature_id = sm.signature_id " +
                                "where cell_line_id = ? and model_system_class = ?";
                        List<Map<String,Object>> signature = jdbcTemplate.getJdbcTemplate().query(signatureSql,
                                new Object[]{rs.getInt("cell_line_id"),"cell line"},
                                new RowMapper<Map<String,Object>>() {
                                    public Map<String,Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                                        Map<String,Object> r = new HashMap<>();
                                        r.put("signature_id",rs.getString("signature_id"));
                                        r.put("assay_category",rs.getString("assay_category"));
                                        if(!categories.containsKey(rs.getString("assay_category")))
                                            categories.put(rs.getString("assay_category"),0);
                                        categories.put(rs.getString("assay_category"),categories.get(rs.getString("assay_category"))+1);
                                        return  r;
                                    }
                                }
                        );
                        cellLine.put("cell_line_id",rs.getInt("cell_line_id"));
                        cellLine.put("cell_line_name",rs.getString("cell_line_name"));
                        cellLine.put("organ",rs.getString("cell_line_organ"));
                        cellLine.put("precursor_cell_name",rs.getString("cell_line_precursor_cell_name"));
                        cellLine.put("tissue",rs.getString("cell_line_tissue").split(";"));
                        cellLine.put("signature",signature);
                        cellLine.put("signature_count",signature.size());
                        cellLine.put("signature_category_count",categories);
                        return cellLine;
                    }
                }
        );
        return SmallMolecules;
    }
}
