package info.ata4.bspinfo.cli;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.KeyValue;
import info.ata4.bsplib.struct.BspData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BspInfoEntitiesToJson {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <input_file> <json_file>");
            System.exit(1);
        }

        String inputPathname = args[0];
        String outputPathname = args[1];

        try {
            BspFile bspFile = new BspFile();
            bspFile.load(Paths.get(inputPathname));

            BspFileReader bspReader = new BspFileReader(bspFile);
            bspReader.loadEntities();

            BspData data = bspReader.getData();
            listEntityTypesAndCounts(data, bspReader.getEntityClassSet());

            JSONArray jsonArray = toJsonArray(data.entities);
            Files.write(Paths.get(outputPathname), jsonArray.toJSONString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void listEntityTypesAndCounts(BspData data, Set<String> entityClassSet) {
        List<Entity> entities = data.entities;
        List<String> entityStrings = new ArrayList<>();

        // create non-unique list of all entity classes
        for (Entity ent : entities) {
            entityStrings.add(ent.getClassName());
        }

        // create rows and count occurrences of all unique entity classes
        for (String cls : entityClassSet) {
            List<Object> row = new ArrayList<>();
            row.add(cls);
            row.add(Collections.frequency(entityStrings, cls));
            System.out.println(row);
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONArray toJsonArray(List<Entity> entities) {
        JSONArray entitiesAsJson = new JSONArray();

        for (Entity entity : entities) {
            JSONObject entityObject = new JSONObject();

            entityObject.put("classname", entity.getClassName());

            for (String key : entity.getKeys()) {
                String value = entity.getValue(key);
                if (key.equals("classname")) {
                    continue;
                }
                entityObject.put(key, value);
            }

            for (KeyValue kv : entity.getIO()) {
                entityObject.put(kv.getKey(), kv.getValue());
            }
            entitiesAsJson.add(entityObject);
        }

        return entitiesAsJson;
    }
}
