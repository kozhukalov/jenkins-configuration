/*
* add jars to classpath
*
* Helper script to dynamically add external libraries to the classpath of
* the process running the Jenkins init scripts.
*
*/

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import java.util.regex.Matcher
import java.util.logging.Logger
import jenkins.model.*

Logger logger = Logger.getLogger("")

def groovyClassLoader = this.class.classLoader
def rootClassLoader = DefaultGroovyMethods.getRootLoader(groovyClassLoader)

if (rootClassLoader == null) {
    rootClassLoader = groovyClassLoader
    ClassLoader parentClassLoader = rootClassLoader.getParent()
    // climb the class hierarchy until you reach the java root loader
    while (parentClassLoader != null) {
        rootClassLoader = parentClassLoader
        parentClassLoader = parentClassLoader.getParent()
    }
}

String jenkinsHome = System.getenv()['JENKINS_HOME']

logger.info("Checking for jars in ${jenkinsHome}/utils")
File jarPath = new File("${jenkinsHome}/utils")

if (!jarPath.exists()) {
    logger.info("Path to jars does not exist. Exiting..")
    System.exit(1)
}

jarPath.eachFile() { file ->
    fileName = file.getName()
    Matcher matcher = file.getName() =~ /.*\.jar/
    if (matcher) {
        logger.info("Adding ${fileName} to the classpath")
        try {
            rootClassLoader.addURL(file.toURL())
            logger.info("Successfully added ${fileName}")
        }
        catch (Exception e) {
            logger.severe("Unable to load ${fileName}")
            logger.severe("Cancelling Jenkins start up.")
            Jenkins jenkins = Jenkins.getInstance()
            jenkins.doSafeExit(null)
            System.exit(1)
        }
    }
}

logger.info("Done adding jars to class path")
