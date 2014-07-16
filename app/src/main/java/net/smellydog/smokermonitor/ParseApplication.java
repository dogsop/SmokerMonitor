package net.smellydog.smokermonitor;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Created by Kenneth.Brown on 7/16/2014.
 */
public class ParseApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(this, "LPO7xM2lmiSxjklrtU2qGRU0ZGYEyismSvUmO55X", "JRuJ8wyQY0ZBFiILdAOULd9GiB7gSf1lSzEz3vKv");

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this line.
        defaultACL.setPublicReadAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);
    }

}
