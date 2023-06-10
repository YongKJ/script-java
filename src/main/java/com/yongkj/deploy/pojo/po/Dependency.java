package com.yongkj.deploy.pojo.po;

import com.yongkj.util.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dependency {

    private String xmlText;
    private String groupId;
    private String artifactId;
    private String version;
    private String jarPath;
    private List<String> packageNames;

    public Dependency() {
        this.xmlText = "";
        this.groupId = "";
        this.artifactId = "";
        this.version = "";
        this.jarPath = "";
        this.packageNames = new ArrayList<>();
    }

    public Dependency(String xmlText, String groupId, String artifactId, String version, String jarPath, List<String> packageNames) {
        this.xmlText = xmlText;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.jarPath = jarPath;
        this.packageNames = packageNames;
    }

    public static Dependency of(String xmlText, String groupId, String artifactId, String version, String jarPath, List<String> packageNames) {
        return new Dependency(xmlText, groupId, artifactId, version, jarPath, packageNames);
    }

    public static List<Dependency> get(String repositoryPath) {
        String pomPath = FileUtil.getAbsPath(false, "pom.xml");
        String regStr = "(\r\n\\s+<dependency>[\\s\\S]*?</dependency>)";
        String pomContent = FileUtil.read(pomPath);
        Pattern pattern = Pattern.compile(regStr);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            List<Dependency> dependencies = new ArrayList<>();
            Matcher matcher = pattern.matcher(pomContent);
            while (matcher.find()) {
                StringReader reader = new StringReader(matcher.group(1));
                InputSource source = new InputSource(reader);
                Document document = builder.parse(source);
                Dependency dependency = new Dependency();
                dependency.setXmlText(matcher.group(1));
                analyzeDocument(document, dependency);
                packData(repositoryPath, dependency);
                dependencies.add(dependency);
            }
            return dependencies;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void packData(String repositoryPath, Dependency dependency) {
        String separator = !System.getProperty("os.name").contains("dows") ? "/" : "\\";
        repositoryPath += separator + dependency.getGroupId().replace(".", separator);
        repositoryPath += separator + dependency.getArtifactId();
        repositoryPath += separator + dependency.getVersion();
        List<File> lstFile = FileUtil.list(repositoryPath);
        try {
            JarInputStream jarInputStream = null;
            for (File file : lstFile) {
                if (file.getName().contains(".sha1")) continue;
                if (!file.getName().contains(".jar")) continue;
                if (file.getName().contains("sources")) continue;
                if (file.getName().contains("javadoc")) continue;
                InputStream inputStream = new FileInputStream(file.getAbsolutePath());
                jarInputStream = new JarInputStream(inputStream);
                dependency.setJarPath(file.getAbsolutePath());
                break;
            }
            JarEntry entry;
            if (jarInputStream == null) return;
            List<String> packageNames = new ArrayList<>();
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.isDirectory()) continue;
                if (entry.getName().contains("META-INF")) continue;
                String[] groupIds = dependency.getGroupId().split("\\.");
                String[] paths = entry.getName().split("/");
                if (paths.length != groupIds.length + 1) continue;

                int index = entry.getName().length() - 1;
                String packageName = entry.getName().substring(0, index);
                packageNames.add(packageName.replace("/", "."));
            }
            dependency.setPackageNames(packageNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeDocument(Document document, Dependency dependency) {
        Element rootEl = document.getDocumentElement();
        NodeList nodeList = rootEl.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            String name = node.getNodeName();
            String value = node.getFirstChild().getNodeValue();
            switch (name) {
                case "groupId":
                    dependency.setGroupId(value);
                    break;
                case "artifactId":
                    dependency.setArtifactId(value);
                    break;
                case "version":
                    dependency.setVersion(value);
                    break;
            }
        }
    }

    public String getXmlText() {
        return xmlText;
    }

    public void setXmlText(String xmlText) {
        this.xmlText = xmlText;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public List<String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(List<String> packageNames) {
        this.packageNames = packageNames;
    }
}
