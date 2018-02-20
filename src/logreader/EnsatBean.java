/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logreader;

/**
 *
 * @author Vedant
 */

import java.util.HashMap;
        
public class EnsatBean {
    
   private String ensat_id;
   private String username;
   private String table;
   private String date;
   private HashMap<String,String> history;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
   
    public String getEnsat_id() {
        return ensat_id;
    }

    public void setEnsat_id(String ensat_id) {
        this.ensat_id = ensat_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public HashMap<String, String> getHistory() {
        return history;
    }

    public void setHistory(HashMap<String, String> history) {
        this.history = history;
    }
   
   
}
