package com.example.superordenata.trackingmap;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class Db4o {

    private ObjectContainer objectContainer;

    public EmbeddedConfiguration getDb4oConfig() throws IOException {
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().add(new AndroidSupport());
        configuration.common().objectClass(TrackingObject.class).
                objectField("date").indexed(true);
        return configuration;
    }

    private ObjectContainer openDataBase(String ruta) {
        try {
            objectContainer = Db4oEmbedded.openFile(getDb4oConfig(), ruta);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectContainer;
    }

    public void addTracking(TrackingObject trackingObject, String ruta){
        objectContainer = openDataBase(ruta);

        objectContainer.store(trackingObject);
        objectContainer.commit();

        objectContainer.close();
    }

    public ArrayList<TrackingObject> getAllLocation(String ruta){
        objectContainer = openDataBase(ruta);
        ArrayList<TrackingObject> result = new ArrayList<TrackingObject>();

        Query consulta = objectContainer.query();
        consulta.constrain(TrackingObject.class);
        ObjectSet<TrackingObject> localizaciones = consulta.execute();
        for(TrackingObject localizacion : localizaciones){
            result.add(localizacion);
        }

        objectContainer.close();

        return result;
    }

    public ArrayList<TrackingObject> getLocationByDate(final String date, String ruta){
        objectContainer = openDataBase(ruta);
        ArrayList<TrackingObject> result = new ArrayList<TrackingObject>();

        ObjectSet<TrackingObject> locs = objectContainer.query(
                new Predicate<TrackingObject>() {
                    @Override
                    public boolean match(TrackingObject loc) {
                        return loc.getDate().equals(date);
                    }
                });

        for(TrackingObject localizacion: locs){
            result.add(localizacion);
        }

        objectContainer.close();

        return result;
    }


}
