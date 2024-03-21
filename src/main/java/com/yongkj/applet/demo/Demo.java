package com.yongkj.applet.demo;

import com.yongkj.AppTest;
import com.yongkj.deploy.pojo.dto.BuildConfig;
import com.yongkj.deploy.pojo.po.Dependency;
import com.yongkj.deploy.pojo.po.PomXml;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.deploy.pojo.po.SourceCode;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo {

    private final String msg;

    private Demo() {
        this.msg = GenUtil.getValue("msg");
    }

    private void test() {
        Map<String, Object> data = new HashMap<>();
        data.put("msg", "世界，你好！");
        GenUtil.writeConfig("demo.yaml", data);
        LogUtil.loggerLine(Log.of("Demo", "test", "msg", msg));
        final String dir = System.getProperty("user.dir");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "dir", dir));
        String path = AppTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "path", path));
        path = FileUtil.dirname(path);
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "path", path));
        path = FileUtil.appDir();
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "appDir", path));
        path = FileUtil.getAbsPath(false, "src", "main", "resources");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "absPath", path));
        path = FileUtil.getAbsPath(true, "src", "main", "resources");
        System.out.println("----------------------------------------------------------------- ");
        LogUtil.loggerLine(Log.of("Demo", "test", "absPath", path));
    }

    private void test1() {
        String humpName = GenUtil.toHump("build-script-service.java");
        LogUtil.loggerLine(Log.of("Demo", "test1", "humpName", humpName));
        System.out.println("----------------------------------------------------------------- ");
        String lineName = GenUtil.toLine("buildScriptService.java");
        LogUtil.loggerLine(Log.of("Demo", "test1", "lineName", lineName));
        System.out.println("----------------------------------------------------------------- ");
    }

    private void test2() {
        String url = "http://localhost:8899/path/demo";
        String data = ApiUtil.requestByGetWithParams(url, new HashMap<>());
        LogUtil.loggerLine(Log.of("Demo", "test2", "data", data));
        System.out.println("----------------------------------------------------------------- ");
        ApiUtil.setProxyEnable(true);
        url = "https://www.google.com";
        data = ApiUtil.requestByGetWithParams(url, new HashMap<>());
        LogUtil.loggerLine(Log.of("Demo", "test2", "data", data));
        System.out.println("----------------------------------------------------------------- ");
    }

    private void test3() {
        String path = "D:\\Document\\MyCodes\\Github\\script-java\\pom.xml";
        FileUtil.modFile(path, "(<plugins>[\\s\\S]*?</plugins>)", PomXml.getExternalLibraries());
    }

    private void test4() {
        RemoteUtil.execLocalCmd("wmic process list full");
    }

    private void test5() {
        String jarPath = "C:\\Users\\admin\\.m2\\repository\\org\\springframework\\spring-web\\5.3.27\\spring-web-5.3.27.jar";
        try {
            JarEntry entry;
            FileInputStream inputStream = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(inputStream);
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.isDirectory()) continue;
                String[] paths = entry.getName().split("/");
                if (paths.length != 3) continue;
                int index = entry.getName().length() - 1;
                String packageName = entry.getName().substring(0, index);
                packageName = packageName.replace("/", ".");
                LogUtil.loggerLine(Log.of("Demo", "test5", "entryName", entry.getName()));
                LogUtil.loggerLine(Log.of("Demo", "test5", "packageName", packageName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test6() {
        String regStr = "\n(\\s+<dependency>[\\s\\S]*?</dependency>)";
        String path = "D:\\Document\\MyCodes\\Github\\script-java\\pom.xml";

        String content = FileUtil.read(path);
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(content);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            while (matcher.find()) {
                String xmlData = matcher.group(1).replaceAll("\r\n", "\n");
                LogUtil.loggerLine(Log.of("Demo", "test6", "dependency", "\n" + xmlData));

                Document document = builder.parse(new InputSource(new StringReader(xmlData)));
//                Document document = builder.parse(xmlData);
                NodeList dependencyData = document.getElementsByTagName("dependency");
                String dataStr = dependencyData.item(0).toString();
                LogUtil.loggerLine(Log.of("Demo", "test6", "dataStr", dataStr));


                for (int i = 0; i < dependencyData.getLength(); i++) {
                    NodeList data = dependencyData.item(i).getChildNodes();
                    LogUtil.loggerLine(Log.of("Demo", "test6", "dataLength", data.getLength()));
                    LogUtil.loggerLine(Log.of("Demo", "test6", "data", data.getLength()));
                    for (int j = 0; j < data.getLength(); j++) {
                        Node node = data.item(i);
                        String name = node.getNodeName();
                        String value = node.getNodeValue();
                        LogUtil.loggerLine(Log.of("Demo", "test6", "node", node.toString()));
                        LogUtil.loggerLine(Log.of("Demo", "test6", "name", name));
                        LogUtil.loggerLine(Log.of("Demo", "test6", "value", value));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test7() {
        String path = "C:\\Users\\admin\\.m2\\repository";
        String separator = "\\";
        String xmlData = "<dependency>\n" +
                "            <groupId>org.apache.httpcomponents</groupId>\n" +
                "            <artifactId>httpclient</artifactId>\n" +
                "            <version>4.5.14</version>\n" +
                "        </dependency>";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document document = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            Document document = builder.parse(new InputSource(new StringReader(xmlData)));
            Element rootEl = document.getDocumentElement();
            NodeList data = rootEl.getChildNodes();

            for (int i = 0; i < data.getLength(); i++) {
                Node node = data.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                String name = node.getNodeName();
                String value = node.getFirstChild().getNodeValue();
                LogUtil.loggerLine(Log.of("Demo", "test6", "name", name));
                LogUtil.loggerLine(Log.of("Demo", "test6", "value", value));
                if (!name.equals("groupId")) continue;
                String[] paths = value.split("\\.");
                for (String pathStr : paths) {
                    path += separator + pathStr;
                }
                LogUtil.loggerLine(Log.of("Demo", "test6", "path", path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test8() {
        String path = "C:\\Users\\admin\\.m2\\repository";
        List<Dependency> dependencies = Dependency.get(path);
        for (Dependency dependency : dependencies) {
            LogUtil.loggerLine(Log.of("Demo", "test6", "dependency", dependency.toString()));
        }
    }

    private void test9() {
        String path = FileUtil.getAbsPath(false, "src", "main", "java");
        String packageName = "com.yongkj.applet.tomcatService.TomcatService";
        String separator = path.contains("/") ? "/" : "\\";
        path += separator + packageName.replace(".", separator);
        path += ".java";

        String regStr = "import (.*);";
        String content = FileUtil.read(path);
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.contains("java.")) continue;
            LogUtil.loggerLine(Log.of("Demo", "test6", "packageName", name));
        }
    }

    private void test10() {
//        Set<String> paths = new HashSet<>();
//        String packageName = "com.yongkj.applet.tomcatService.TomcatService";
//        String packageName = "com.yongkj.deploy.service.BuildScriptService";
//        String packageName = "com.yongkj.applet.librariesPull.LibrariesPull";
//        String packageName = "com.yongkj.applet.springTest.SpringTest";
//        String content = FileUtil.read(Script.getSourceCodePath(packageName));
//        Set<String> packageNames = Script.analyzeExternalPackageName(content, packageName, paths, true);
//        for (String name : packageNames) {
//            LogUtil.loggerLine(Log.of("Demo", "test10", "name", name));
//        }
//        for (String path : paths) {
//            LogUtil.loggerLine(Log.of("Demo", "test10", "path", path));
//        }

//        List<Script> lstScript = Script.get();
//        for (Script script : lstScript) {
//            LogUtil.loggerLine(Log.of("Demo", "test10", "script", script.toString()));
//        }
    }

    private void test11() {
        BuildConfig config = BuildConfig.get();
        LogUtil.loggerLine(Log.of("Demo", "test11", "pomDependenciesOriginal", config.getPomDependenciesOriginal()));
    }

    private void test12() {
        String path = "C:\\Users\\admin\\.m2\\repository";
        List<Dependency> dependencies = Dependency.get(path);
        BuildConfig config = BuildConfig.get();
        List<Script> lstScript = Script.get();
        for (Script script : lstScript) {
            String pomDependenciesLatest = BuildConfig.getPomDependenciesLatest(dependencies, script, null);
            LogUtil.loggerLine(Log.of("Demo", "test12", "scriptName", script.getScriptName()));
            LogUtil.loggerLine(Log.of("Demo", "test12", "pomDependenciesLatest", pomDependenciesLatest));
        }
    }

    private void test13() {
        String pomPath = "C:\\Users\\admin\\Desktop\\pom.xml";
        String mainFest = "C:\\Users\\admin\\Desktop\\MANIFEST.MF";
        String jarPath = "D:\\Document\\MyCodes\\Github\\script-java\\script\\tomcat-service.jar";
        try {
            JarEntry entry;
//            JarFile jarFile = new JarFile(jarPath);
            JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarPath));
//            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath));

//            Attributes attributes = jarInputStream.getManifest().getMainAttributes();
//            String classPathOriginal = attributes.getValue("Class-Path");
//            String classPathLatest = classPathOriginal.replace("..", ".");
//            LogUtil.loggerLine(Log.of("Demo", "test13", "classPathOriginal", classPathOriginal));
//            LogUtil.loggerLine(Log.of("Demo", "test13", "classPathLatest", classPathLatest));

            while ((entry = jarInputStream.getNextJarEntry()) != null) {
//                if (entry.isDirectory()) continue;
//                if (!entry.getName().contains("pom.xml")) continue;
//                if (!entry.getName().contains("MANIFEST.MF")) continue;
                LogUtil.loggerLine(Log.of("Demo", "test13", "entryName", entry.getName()));

//                InputStream stream = jarFile.getInputStream(entry);
//                OutputStream outputStream = new FileOutputStream(pomPath);
//                OutputStream outputStream = new FileOutputStream(mainFest);
//                int len;
//                byte[] b = new byte[1024];
//                while ((len = stream.read(b)) > 0) {
//                    outputStream.write(b, 0, len);
//                }
//                outputStream.flush();
//                stream.close();

//                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test14() {
        String regStr = "<classpathPrefix>([\\s\\S]*?)</classpathPrefix>";
        String pomPath = "C:\\Users\\admin\\Desktop\\pom.xml";
        FileUtil.modContent(pomPath, regStr, "./libs/");
    }

    private void test15() {
        String srcFolder = "C:\\Users\\admin\\Desktop\\script-java";
        String desFolder = "C:\\Users\\admin\\Desktop\\test\\script-java";
        FileUtil.move(srcFolder, desFolder);
    }

    private void test16() {
        String name = Demo.class.getSimpleName();
        String lineName = GenUtil.toLine(name);
        String profileName = GenUtil.getProfile();
        LogUtil.loggerLine(Log.of("Demo", "test16", "name", name));
        LogUtil.loggerLine(Log.of("Demo", "test16", "lineName", lineName));
        LogUtil.loggerLine(Log.of("Demo", "test16", "profileName", profileName));
    }

    private void test17() {
        Map<String, SourceCode> codes = SourceCode.get();
        List<Script> lstScript = Script.get();
        LogUtil.loggerLine(Log.of("Demo", "test17", "codes.size()", codes.size()));
        LogUtil.loggerLine(Log.of("Demo", "test17", "lstScript.size()", lstScript.size()));
    }

    private void test18() {
        String content = "Hello world!";
//        String key = AesUtil.generateKey();
        String key = "GuWpAYNaAn7aDOutTGyQrlU7N3zCWiSDDnoWCuldtZs=";
        LogUtil.loggerLine(Log.of("Demo", "test18", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test18", "key", key));
//        String encryptStr = AesUtil.aesEncrypt(key, content);
        String encryptStr = "lXO5FgiOAP4QgoWPENbhzQ==";
        LogUtil.loggerLine(Log.of("Demo", "test18", "encryptStr", encryptStr));
        String decryptStr = AesUtil.aesDecrypt(key, encryptStr);
        LogUtil.loggerLine(Log.of("Demo", "test18", "decryptStr", decryptStr));
    }

    private void test19() {
        String content = "Hello world!";
        Map<String, String> keyMap = RsaUtil.generateKey();
        String publicKey = keyMap.get(RsaUtil.KEY_TYPE_PUBLIC_KEY);
        String privateKey = keyMap.get(RsaUtil.KEY_TYPE_PRIVATE_KEY);
        LogUtil.loggerLine(Log.of("Demo", "test19", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test19", "publicKey", publicKey));
        LogUtil.loggerLine(Log.of("Demo", "test19", "privateKey", privateKey));
        System.out.println("----------------------------------------------------------------------------------");

        String privateKeyEncryptStr = RsaUtil.rsaEncrypt(privateKey, content, RsaUtil.KEY_TYPE_PRIVATE_KEY);
        String publicKeyDecryptStr = RsaUtil.rsaDecrypt(publicKey, privateKeyEncryptStr, RsaUtil.KEY_TYPE_PUBLIC_KEY);
        LogUtil.loggerLine(Log.of("Demo", "test19", "privateKeyEncryptStr", privateKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test19", "publicKeyDecryptStr", publicKeyDecryptStr));
        System.out.println("----------------------------------------------------------------------------------");

        String publicKeyEncryptStr = RsaUtil.rsaEncrypt(publicKey, content, RsaUtil.KEY_TYPE_PUBLIC_KEY);
        String privateKeyDecryptStr = RsaUtil.rsaDecrypt(privateKey, publicKeyEncryptStr, RsaUtil.KEY_TYPE_PRIVATE_KEY);
        LogUtil.loggerLine(Log.of("Demo", "test19", "publicKeyEncryptStr", publicKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test19", "privateKeyDecryptStr", privateKeyDecryptStr));
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void test20() {
        String content = "Hello world!";
        Map<String, String> keyMap = SignatureUtil.generateKey();
        String publicKey = keyMap.get(SignatureUtil.KEY_TYPE_PUBLIC_KEY);
        String privateKey = keyMap.get(SignatureUtil.KEY_TYPE_PRIVATE_KEY);
        LogUtil.loggerLine(Log.of("Demo", "test20", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test20", "publicKey", publicKey));
        LogUtil.loggerLine(Log.of("Demo", "test20", "privateKey", privateKey));
        System.out.println("----------------------------------------------------------------------------------");

        String privateKeyEncryptStr = SignatureUtil.generateSignature(privateKey, content);
        boolean publicKeyDecryptResult = SignatureUtil.verifySignature(publicKey, content, privateKeyEncryptStr);
        boolean publicKeyDecryptResultTest = SignatureUtil.verifySignature(publicKey, content + " ", privateKeyEncryptStr);
        LogUtil.loggerLine(Log.of("Demo", "test20", "privateKeyEncryptStr", privateKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test20", "publicKeyDecryptResult", publicKeyDecryptResult));
        LogUtil.loggerLine(Log.of("Demo", "test20", "publicKeyDecryptResultTest", publicKeyDecryptResultTest));
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void test21() {
        String privateKeyEncryptStr = "aOuk8Fpz99Q4fIke/1pDxqgbF2l5ghTh+CPvE8Q4iUD/by41Htcd1ioMPfXjwc85HLuTNsTvAJ80uIyMN14Zpvgoyu1Fw3mjTlACQRcGgSBH8nfzc+mb36oXaOpt4ZsAE3Z/E9j7dC9krAFWsZN8akRgRWgBFOWMfMiei1UleAFIbykYpG90imlg61l4m24GUDD/QrOdz97D1PyWjgs0lZVkQFF/O8Q0S2h99WR/r+bUOjO0IzaWedHsW/+k1WOXenbMY9HV/c/lVbuUBRQYNa7qECMqS18KkDRp+lWk56g//d3DBtHjqe7PvjMrMD1IKdiaLsm10YzontIk2VvtTQ==";
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArCS+Mc95KdU5XvsL5Z5sYo78CmRgZ2D65dMHaZw8KjaJtM2SXiswz3f6ykR+dgSRONQifQjfdLdiaAiczJqchRLb2y5FrlQJ/Rxe+Z3Jn74vuUiKn5XeMy4nqsdNaaLQavakfQBMYmAPcxZfnjNIyyfyMGIFmW+MMgs/bGXLO6UrwJvaaXC59x5+2I/5KL0TrL9kNWBh0Xfv61U64fg0cAqHZF2HzckmxSF8H3AEAheqbvzuqzkBW26dP+2x5MOKQmfbkf8VlX+lcqPdvIFZOYQQ+olHsGIUzduuk8Xt6UqfMwDo4PTnv9SVQQdFlZ/hLOtncTMLoeqPM8H2rJ3ihwIDAQAB";
        String publicKeyDecryptStr = RsaUtil.rsaDecrypt(publicKey, privateKeyEncryptStr, RsaUtil.KEY_TYPE_PUBLIC_KEY);

        LogUtil.loggerLine(Log.of("Demo", "test19", "publicKey", publicKey));
        System.out.println("----------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("Demo", "test21", "privateKeyEncryptStr", privateKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test19", "publicKeyDecryptStr", publicKeyDecryptStr));
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void test22() {
        String ivStr = AesUtil.generateIV();
        LogUtil.loggerLine(Log.of("Demo", "test22", "ivStr", ivStr));
    }

    private void test23() {
        String content = "Hello world!";
        String ivStr = "iVf3rzor4buyLwG4";
        String key = "GuWpAYNaAn7aDOutTGyQrlU7N3zCWiSDDnoWCuldtZs=";

        LogUtil.loggerLine(Log.of("Demo", "test18", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test18", "ivStr", ivStr));
        LogUtil.loggerLine(Log.of("Demo", "test18", "key", key));
//        String encryptStr = AesUtil.aesEncrypt(key, ivStr, content);
        String encryptStr = "GigvhLC75KwiDhi3Lg91yg==";
        LogUtil.loggerLine(Log.of("Demo", "test18", "encryptStr", encryptStr));
        String decryptStr = AesUtil.aesDecrypt(key, ivStr, encryptStr);
        LogUtil.loggerLine(Log.of("Demo", "test18", "decryptStr", decryptStr));
    }

    private void test24() {
        String content = "Hello world!";
        String iv = AesUtil.generateIV();
        String key = AesUtil.generateKey();

        LogUtil.loggerLine(Log.of("Demo", "test18", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test18", "iv", iv));
        LogUtil.loggerLine(Log.of("Demo", "test18", "key", key));
        System.out.println("-----------------------------------------------------------------------------");

        String encryptStr = AesUtil.aesEncrypt(key, iv, content);
        LogUtil.loggerLine(Log.of("Demo", "test18", "encryptStr", encryptStr));
        String decryptStr = AesUtil.aesDecrypt(key, iv, encryptStr);
        LogUtil.loggerLine(Log.of("Demo", "test18", "decryptStr", decryptStr));
    }

    private void test25() {
        String content = "Hello world!";
        String iv = "wmgcCFxfuL2MaegK";
        String key = "ODJrHhcGN1DGglwIZ4f7YE5I7sg5JxUspHFRFi3S+lk=";

        LogUtil.loggerLine(Log.of("Demo", "test18", "content", content));
        LogUtil.loggerLine(Log.of("Demo", "test18", "iv", iv));
        LogUtil.loggerLine(Log.of("Demo", "test18", "key", key));
        System.out.println("-----------------------------------------------------------------------------");

        String encryptStr = "izMCHqcvIxEHIMoaFp4qiw==";
        String encryptStrLatest = AesUtil.aesEncrypt(key, iv, content);
        LogUtil.loggerLine(Log.of("Demo", "test18", "encryptStrLatest", encryptStrLatest));
        LogUtil.loggerLine(Log.of("Demo", "test18", "encryptStr", encryptStr));
        String decryptStr = AesUtil.aesDecrypt(key, iv, encryptStrLatest);
        LogUtil.loggerLine(Log.of("Demo", "test18", "decryptStr", decryptStr));
    }

    private void test26() {
        String base64Iv = "V1lhZjI2b3c3WTlmWjFpeg==";
        String base64Key = "Ep0U1U2zm7vfA1UZ03m2+E1pU/ugLEZ33tO1ui7rT2s=";
        String base64Content = "FbcG8w9oKVTfvxdt6j74vA==";
        String decryptStr = AesUtil.aesDecrypt(base64Key, base64Iv, base64Content);
        LogUtil.loggerLine(Log.of("Demo", "test26", "decryptStr", decryptStr));
    }

    private void test27() {
        String content = "Hello world!";
        String base64Iv = "V1lhZjI2b3c3WTlmWjFpeg==";
        String base64Key = "Ep0U1U2zm7vfA1UZ03m2+E1pU/ugLEZ33tO1ui7rT2s=";
        String encryptStr = AesUtil.aesEncrypt(base64Key, base64Iv, content);
        LogUtil.loggerLine(Log.of("Demo", "test26", "encryptStr", encryptStr));
    }

    public static void run(String[] args) {
        Demo demo = new Demo();
//        demo.test27();
//        demo.test26();
//        demo.test25();
//        demo.test24();
//        demo.test23();
//        demo.test22();
        demo.test21();
//        demo.test20();
//        demo.test19();
//        demo.test18();
//        demo.test17();
//        demo.test16();
//        demo.test15();
//        demo.test14();
//        demo.test13();
//        demo.test12();
//        demo.test11();
//        demo.test10();
//        demo.test9();
//        demo.test8();
//        demo.test7();
//        demo.test6();
//        demo.test5();
//        demo.test4();
//        demo.test3();
//        demo.test2();
//        demo.test1();
//        demo.test();
    }

}
