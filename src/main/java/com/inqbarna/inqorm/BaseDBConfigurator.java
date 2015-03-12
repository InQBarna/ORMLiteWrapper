package com.inqbarna.inqorm;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;

/**
 * Created by David García <david.garcia@inqbarna.com> on 12/3/15.
 */
public class BaseDBConfigurator extends OrmLiteConfigUtil {

    protected static void doSetupForClassloader(String [] args) {
        if (args.length != 1) {
            System.err.println("Se requiere un argumento");
            return;
        }

        File androidJar = new File(args[0]);
        if (!androidJar.exists() || !androidJar.isFile()) {
            System.err.println("El argumento no es un Android Jar válido: " + args[0]);
            return;
        }

        try {
            Method m;
            m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            m.setAccessible(true);
            m.invoke(ClassLoader.getSystemClassLoader(), androidJar.toURI().toURL());
        } catch (NoSuchMethodException | MalformedURLException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected static void doGenerateConfig(String outputFilePath, Class<?>[] targetTableClasses) throws IOException, SQLException {
        writeConfigFile(new File(outputFilePath), targetTableClasses);
    }
}
