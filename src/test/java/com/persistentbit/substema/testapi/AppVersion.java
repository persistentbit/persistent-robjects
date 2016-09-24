package com.persistentbit.substema.testapi;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class AppVersion{
    public enum RunEnvironment{
        develop,production
    };

    public final String name;
    public final String version;
    public final RunEnvironment environment;

    public AppVersion(String name, String version, RunEnvironment environment) {
        this.name = name;
        this.version = version;
        this.environment = environment;
    }


    @Override
    public String toString() {
        return "AppVersion{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", environment=" + environment +
                '}';
    }
}
