/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logreader;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 *
 * @author Vedant
 */
public class LogReader {

    public static void main(String[] args) throws IOException {
        //  MongoClient ip and port no
        MongoClient mongoClient = new MongoClient("localhost", 27017);

        // Get handle to "ensat"
        DB db = mongoClient.getDB("ensat");
        // Get collection ensatHistory
        DBCollection collection = db.getCollection("ensatHistory");
        // Get collection ensatLogInfo
        DBCollection collectionInfo = db.getCollection("ensatLogInfo");

        // search for updated string
        String grepfor = "=== RECORD UPDATED ===";

        File inputFile = null;
        int lineNumber = 0;
        int debugSum = 0;
        int errorSqlSum = 0;
        int errorSum = 0;
        int update = 0;
        LineIterator it = null;
        EnsatBean ensat = new EnsatBean();
        EnsatBean ensat1 = new EnsatBean();
        List<String> tableValues = new ArrayList<>();
        List<String> dateValue = new ArrayList<>();
        String logDate = "";
        try {
            // log file path
            inputFile = new File("C:\\Users\\Dhruv\\Desktop\\unimelb\\Subjects\\Sem 1\\Summer Internship\\vedant_logs\\ensat.log.2018-01-19");
            it = FileUtils.lineIterator(inputFile, "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();
                String[] lineSplit = null;
                lineNumber++;
                // code for log information charts
                // Date of the log
                if (logDate.isEmpty()) {
                    String logDateList[] = line.split(" ");
                    logDate = logDateList[0] + " " + logDateList[1] + " " + logDateList[2];
                }
                // SQL debug information
                if (line.contains("sql") || line.contains("sqlAction")) {
                    System.out.println(lineNumber);
                    debugSum++;
                }
                // SQL error information
                if (line.contains("SQL")) {
                    System.out.println(lineNumber);
                    errorSqlSum++;
                }
                // Error information
                if (line.contains("error") || line.contains("Error")) {
                    System.out.println(lineNumber);
                    errorSum++;
                }
                // code for search information
                if (line.contains(" - ")) {
                    lineSplit = line.split(" - ");
                    if (!lineSplit[1].isEmpty() && lineSplit[1].equals(grepfor)) {
                        update++;
                        while (it.hasNext()) {
                            String updatedLine = it.nextLine();
                            String[] updateSplit = updatedLine.split(" ");

                            if (!updateSplit[7].isEmpty() && updateSplit[7].equals("=====")) {
                                break;
                            } else {
                                String[] detailSplit = updatedLine.split(":");
                                // adding date to list
                                dateValue.add(detailSplit[0]);
                                String[] fieldSplit = null;
                                if (detailSplit[2].contains("'")) {
                                    fieldSplit = detailSplit[2].split("'");
                                }
                                if (detailSplit.length == 3) {
                                    continue;
                                } else {
                                    //value from logs
                                    if (detailSplit != null && detailSplit.length > 4) {
                                        if (!detailSplit[4].isEmpty() || !detailSplit[6].isEmpty()) {

                                            String oldValue = detailSplit[4].replace(", new_value", " ");
                                            // adding oldvalue to list
                                            tableValues.add(oldValue);

                                            String newValue = detailSplit[5].replace(']', ' ');
                                            // adding new value to list
                                            tableValues.add(newValue);
                                        } else {
                                            System.out.println("N/A");
                                        }
                                    } else {
                                        // adding ensat id, table name username to list 
                                        tableValues.add(detailSplit[3]);
                                    }

                                    //updated fields from log
                                    if (fieldSplit != null && !fieldSplit[3].isEmpty()) {
                                        // adding field to list
                                        tableValues.add(fieldSplit[3]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // calculating log information
            int errSum = (errorSum - errorSqlSum);
            int remain = lineNumber - (debugSum + errorSqlSum + errSum + update);
            System.out.println("Debug Sum:" + debugSum);
            System.out.println("Error SQL Sum:" + errorSqlSum);
            System.out.println("Error Sum:" + errSum);
            System.out.println("Update:" + update);
            System.out.println("Remaining:" + remain);
            System.out.println("Total:" + lineNumber);

            DBObject logInfo = new BasicDBObject()
                    .append("logDate", logDate)
                    .append("sqlDebugInfo", debugSum)
                    .append("sqlErrorInfo", errorSqlSum)
                    .append("errorInfo", errSum)
                    .append("updatedRecordsInfo", update)
                    .append("remainingInfo", remain);
            // inserting log information in db
            collectionInfo.insert(logInfo);

            // simplifying date value from list 
            String date = "";
            if (!dateValue.isEmpty()) {
                String dateVal = dateValue.get(0);
                String[] dateList = dateVal.split(" ");
                date = " " + dateList[0] + " " + dateList[1] + " " + dateList[2];
            }

            int i = 0;
            List<String> subList1 = new ArrayList<>();
            List<String> subList2 = new ArrayList<>();
            List<Integer> index = new ArrayList<>();
            HashMap<String, String> history = new HashMap<>();
            // simplifying other value from list
            if (!tableValues.isEmpty()) {
                for (String value : tableValues) {
                    String finalValue = value.replaceFirst(" ", "");
                    // adding updated record line number to a list index for simplifying
                    if (finalValue.matches("[(A-Z)\\d-]*")) {
                        index.add(i);
                    }
                    i++;
                }

                // iterating over index list to get line number of updated record except last record
                for (int j = 0; j < index.size() - 1; j++) {
                    subList1 = tableValues.subList(index.get(j), index.get(j + 1));

                    // adding data to the bean
                    ensat.setEnsat_id(subList1.get(0));
                    ensat.setUsername(subList1.get(1));
                    ensat.setTable(subList1.get(2));
                    ensat.setDate(date);
                    for (int k = 3; k < subList1.size(); k = k + 3) {
                        history.put("old_value", subList1.get(k));
                        history.put("new_value", subList1.get(k + 1));
                        history.put("field", subList1.get(k + 2));
                        ensat.setHistory(history);

                        DBObject result = new BasicDBObject()
                                .append("ensat_id", ensat.getEnsat_id())
                                .append("username", ensat.getUsername())
                                .append("table", ensat.getTable())
                                .append("date", ensat.getDate())
                                .append("history", ensat.getHistory());
                        // inserting the data in db except the last record
                        collection.insert(result);
                    }

                    //iterating over index list to get line number of updated record for last record
                    if (subList2.isEmpty()) {
                        subList2 = tableValues.subList(index.get(index.size() - 1), tableValues.size());
                        // adding data to bean
                        ensat1.setEnsat_id(subList2.get(0));
                        ensat1.setUsername(subList2.get(1));
                        ensat1.setTable(subList2.get(2));
                        ensat1.setDate(date);
                        for (int k = 3; k < subList2.size(); k = k + 3) {
                            history.put("old_value", subList2.get(k));
                            history.put("new_value", subList2.get(k + 1));
                            history.put("field", subList2.get(k + 2));
                            ensat1.setHistory(history);

                            DBObject result1 = new BasicDBObject()
                                    .append("ensat_id", ensat1.getEnsat_id())
                                    .append("username", ensat1.getUsername())
                                    .append("table", ensat1.getTable())
                                    .append("date", ensat1.getDate())
                                    .append("history", ensat1.getHistory());
                            // adding last record to db
                            collection.insert(result1);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            it.close();
        }

    }
}
