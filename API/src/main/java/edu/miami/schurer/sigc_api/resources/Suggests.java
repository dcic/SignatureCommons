package edu.miami.schurer.sigc_api.resources;

public class Suggests {

    private final String hit_object_lookup_table;
    private final String hit_object_lookup_table_id_field;
    private final String hit_object_lookup_table_class_field;
    private final String hit_object_class;
    private final String hit_type;
    private final String suggest_term;
    private final int hit_object_id;
    private final String preferred_term;

    public Suggests(String suggest_term,
                    int hit_object_id,
                    String hit_object_lookup_table,
                    String hit_object_lookup_table_id_field,
                    String hit_object_lookup_table_class_field,
                    String hit_object_class,
                    String preferred_term,
                    String hit_type) {
        this.suggest_term = suggest_term;
        this.hit_object_id = hit_object_id;
        this.hit_object_lookup_table = hit_object_lookup_table;
        this.hit_object_lookup_table_id_field = hit_object_lookup_table_id_field;
        this.hit_object_lookup_table_class_field = hit_object_lookup_table_class_field;
        this.hit_object_class = hit_object_class;
        this.preferred_term=preferred_term;
        this.hit_type = hit_type;
    }

    public String getSuggest_term() {
        return suggest_term;
    }

    public int getHit_object_id() {
        return hit_object_id;
    }

    public String getHit_object_lookup_table() {
        return hit_object_lookup_table;
    }

    public String getHit_object_lookup_table_id_field() {
        return hit_object_lookup_table_id_field;
    }

    public String getHit_object_lookup_table_class_field() {
        return hit_object_lookup_table_class_field;
    }

    public String getHit_object_class() {
        return hit_object_class;
    }

    public String getPreferred_term() { return preferred_term; }

    public String getHit_type() { return hit_type; }



}
