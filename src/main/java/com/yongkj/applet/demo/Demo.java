package com.yongkj.applet.demo;

import com.alibaba.fastjson2.JSONObject;
import com.yongkj.AppTest;
import com.yongkj.deploy.pojo.dto.BuildConfig;
import com.yongkj.deploy.pojo.po.Dependency;
import com.yongkj.deploy.pojo.po.PomXml;
import com.yongkj.deploy.pojo.po.Script;
import com.yongkj.deploy.pojo.po.SourceCode;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import reactor.core.publisher.Flux;
import rufus.lzstring4java.LZString;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.util.*;
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

    private void test28() {
        Flux.just(1, 2, 3, 4, 5)
                .log()
                .map(i -> i * 2)
                .subscribe(System.out::println);
    }

    private void test29() {
        String jsonStr = "{\"msg\": \"Hello world!\"}";
        String xmlStr = GenUtil.jsonToXml(jsonStr, Map.class);
        String tempJsonStr = GenUtil.xmlToJson(xmlStr);

        LogUtil.loggerLine(Log.of("Demo", "test29", "jsonStr", jsonStr));
        LogUtil.loggerLine(Log.of("Demo", "test29", "xmlStr", xmlStr));
        LogUtil.loggerLine(Log.of("Demo", "test29", "tempJsonStr", tempJsonStr));
    }

    private void test30() {
        String key = AesUtil.generateKey();
        LogUtil.loggerLine(Log.of("Demo", "test30", "key", key));
    }

    private void test31() {
        String privateKeyEncryptStr = "ZjY7RAZgznIKnCKHJTel+E6y41dzMwIKSm/kEm32XKWPk4qyP/FHq2kU2seP1h1MRXLtZ3ZKJ40N3D6K+vXCfTyGDLfahpnXOAyO9yT35VPw3Zx/aRf+SjCc+79gnHEqfy5vhyAE14vs+uCipX2j4ArD0rYFiKtQg2/9dJxohxqYAw4au0KV9dmuiBf5EFhHUs1o8XGYFJWwFF8HVQgqXdNP2/cSxE5Maltski4jXKMizPegl/ivC8scjDj/36UxC080FKsD+w3lH+MfB7yxfYBGLsZ5PPBAWSFqMHkUS6M9WcR0A19qcYgQ2UOqc10mQWbVGJCddHm0Rhq7Hlat1g==";
        String publicKey = "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQB+gX4DgWY1ZMzuhgVc39pV3zmI0LpsxK6pZDxGvTIU/ML/9W/XvN5cQ0dDiIgc4QvsmYLdaWDOQPE3IO1g9Xt3jXhAUk2/7lz5+lfr/8Oad9hqAj2LXHlYKsueGL6B834CBj2Co+oQcwYM+Tb6MY3ahKaak8pJoULf9S4Yhn3xwh1Rbs0BUQhKo+MvoaPUy1+mtoyhDnXsUaG3e1nNGtwx9a3Ecg90lTC9WVqu5F7ed2mRs6UEijtd/MAgHqBypBWuhTtQU1K57mqCV4t5aUTG/g4xCaWbFtSeobNNChIi5VD0Wd5qSiUWDKN33uETqA4CHEDA58EYG7CKVBzj3x+BAgMBAAE=";
        String publicKeyDecryptStr = RsaUtil.rsaDecrypt(publicKey, privateKeyEncryptStr, RsaUtil.KEY_TYPE_PUBLIC_KEY);

        LogUtil.loggerLine(Log.of("Demo", "test31", "publicKey", publicKey));
        System.out.println("----------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("Demo", "test31", "privateKeyEncryptStr", privateKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test31", "publicKeyDecryptStr", publicKeyDecryptStr));
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void test32() {
        String content = "Hello world";
        String privateKey = "MIIEoQIBAAKCAQB+gX4DgWY1ZMzuhgVc39pV3zmI0LpsxK6pZDxGvTIU/ML/9W/XvN5cQ0dDiIgc4QvsmYLdaWDOQPE3IO1g9Xt3jXhAUk2/7lz5+lfr/8Oad9hqAj2LXHlYKsueGL6B834CBj2Co+oQcwYM+Tb6MY3ahKaak8pJoULf9S4Yhn3xwh1Rbs0BUQhKo+MvoaPUy1+mtoyhDnXsUaG3e1nNGtwx9a3Ecg90lTC9WVqu5F7ed2mRs6UEijtd/MAgHqBypBWuhTtQU1K57mqCV4t5aUTG/g4xCaWbFtSeobNNChIi5VD0Wd5qSiUWDKN33uETqA4CHEDA58EYG7CKVBzj3x+BAgMBAAECggEAGqpE24xM9Pg4Uhahzr/Ij80rnXERupeCkWX1Zpu+kLnuiHE9j+XZoOz1eoCu4Bd3ofmBohXyN8dskMg6APP9gGerbzNp/AjMREzGd5JDsq2MSlwKiPOHCFSM8tgYX9/pMZgLIxxV2/3Cyr1FH1uOoP7Q90grnXWGWRqfCsBej7SjG/sbZ5i/zNTHlgxCm2TQSmaqSKFiJrDRTpS7hGXOT2kSrHPOxnAdpeWZCJqpal9cdiXm1+cS22Fp98Wob3AsPfvRxVDzw1YTK9EbdbBrl5w8vKlwAZxwHwC6IyBkCkHF21N0XLEJPBOJQ93W//I03axcSUvKV4e+2WD9ci4uIQKBgQDhRbyMnrspSW2GqX0mU53ImL2yND56wlC4yy1Ru6xZt+FS+VreyiZ4b136WKGB+WSRYmQLu+SDDL0HB344bxOT359lwH4wePQfxgaCZkotHqbsGivfB6nbpuC3ecQC8LEI9QQ43wQzz0W3MAvhbdiRKer/VI9x8AMVCKUA9kgs5QKBgQCPwu+agub6XZoOMVfiu32BbIEWVVHzFTFaM4pxmuCBRK6TZgutdIZwcAVBT47q4sLh3hjpRWygqYetEManAI0Q6KfxMMAZPfEEPf9vK7vsQVfUuGyk+gKasA6ACOUwnZ5klx+v/ksh/qxvPPso51qlumhLzrYDaSQ9DAvdV7XabQKBgFI4kckBZm57Tm6IDhWfD0fX9+TDDMrhgXTxM2lRhmZ4ptUSDgii38e1XiexkcFOtVYbA0kFGzJ5w8R9B6SxZgPi6trIl0Y532B5dobBrvEo7uPd6YVN1sFE92AAIXqxPux2AUSAoZgHXvqtnqh430KcehkXVAPSBk33CcKAIXfBAoGAGpNLncGDF1dESkle2MAWFF3NgyZkjs4lIGqUtimORPdAWmkr0XXQAGsE3azqW4ttwTlAW6cEzLJqLVkDkA2vBigsG48Kg+AKdOFNHez0wVjW9rDdDKZW4ueW1p5OV+1bQXcaWXtlwjQFBk88krQp8qcB7rmYtgqys0JN0y4KpQUCgYAJ4hZRbgHYSKcTfvsprU6iTd5XQOHAY9vCsWzNEFzyIkI2sO2K4anokFsrytorx3BCYFZrKzodeKxmC/jnO6Mnx/Rz6PDUSI9c/WDZJQz2WIhflo6t2Qee7buuNMhvlvps6XXaNEFCA01/ruT6N1jW1NYf/nANLRzyqEwKhkHgEg==";
        String privateKeyEncryptStr = RsaUtil.rsaEncrypt(privateKey, content, RsaUtil.KEY_TYPE_PRIVATE_KEY);

        LogUtil.loggerLine(Log.of("Demo", "test31", "privateKeyEncryptStr", privateKeyEncryptStr));
    }

    private void test33() {
        Map<String, String> keyPair = RsaUtil.generateKey();
        LogUtil.loggerLine(Log.of("Demo", "test33", "privateKey", keyPair.get(RsaUtil.KEY_TYPE_PRIVATE_KEY)));
        LogUtil.loggerLine(Log.of("Demo", "test33", "publicKey", keyPair.get(RsaUtil.KEY_TYPE_PUBLIC_KEY)));

//        Map<String, String> keyPair = new HashMap<>();
//        keyPair.put(RsaUtil.KEY_TYPE_PRIVATE_KEY, "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCLVVzGq2MEmlyOe9x3k80bZEWMuZVU9VWJGO39p4sEKp1D56ThNBr/tFX6ruOBoymE3VVuGV7fDU+WMK7F4+/hIiAnBwral1UQm/T1vlHyQP+vEFbxvkjoPDNUv6pxEXO9G8JF53n+6HeF7E/A/kDwlCP678krVMidz/jrotar0vHp1LSevpl7J6aFHaMb6ttXlo/y9kC5+oEp1TrivnBdfgjxfUME/ZViATsm51HD+EWi3KCMJRICyInfCPrp7wPoaZeaFHmPic1sEXZFvPGp+rTygvWbJmFC6NoPWbT8RMcG4Dwx8y2xzoMNOrINgrxy6aRVPq4nCsSjC6EZjAiNAgMBAAECggEAFgGqgJprLjvvKHX5sU0/1Pa2YW6iXmPNWbT9SdXfiA3u/t2kVglzxjVGayyaNEx/70NCDfyl1lb03gTcZdcFeSp5p5BeL349aj0nqHSh3sBTYeQTx2TFgSCY/uay1d7qx9CNk5rm0tMGpblYM2Q0/4p2UFj1Dgn7EanaZ+U1QLf3Qhhm35SbS9b27gghANgoPG0lRKkXYqTems5WDOoVz9t4G+gfYVp9Vvk1FlhNgV54s7KgUDDc31acIX4no9P9+5g5tOWA0tSXJyrdautoAMAFAc3tcW9SgRJAcgXFr1sixIqqoWa9tzf1iJdesC+qa2kdNqH7FyiBFuOnOCbVhQKBgQD91zcRX2olhhmv9DJCmIJ3M99O59CM1LXWeNerraenX0gg/G1ZeqY40RwwTkFw2lsx34PZybPXifmxF1oV6ZU4OAMtFw3K7N8Ag3Zl6oOO+WqWjowTp+Gh0U98E9BtHYdJ8seI79pJgU8ghkfYTI95GbF3yD1lLNiCxcNd4oSNhwKBgQCMhMljoykmgrZkpd2stoZQlj1oUpbASvWkZIusKYktTptoG9YIUeGrhgYbgH5o/uIzfWvrK2Acw4+FgVBXVmCgWCBxKWR49XvvEcJZMnhngjCo2BUS/BQzWjvEz6G5HURS6yeI/VuqJQewRUmta53MoipVXV3WtmQ1qW0C8QleSwKBgQD8jwMVX+/GwfQQ41CWfhNg8tV+NpuDY6hAah97it5CY0TqvP03iBaWdUgsr8/grwEJUDBpoowzKe6rSmaxYqS7OM/ALy3j92WxpxtIL9sRiAuLHO02KVUPoagJ9dTsm/Kdmtb44lw3wlauL6yQAyzE+tFeMncTkVkiVA4b/WKRRQKBgCZGSQCtwZiuw/WYRFA2C1HKCoHu9Qb9o/kaNf039xhzL9tksaDkhL6Oq/0zu7rnqj9yK0IMN4q+LcqklXikm38xYezQ5IAaBZ7xzGk1WRyAlrZ23Q6lwO7X5i3OjHN6qFCZotQqhpxKp9R4mv/0ry+9ACDuZJAGcDc5perGMtIPAoGAf4Z0XFvNwexPj8DIRdcu+CeeQZRIW5eBt7x8uFw89XSQpoFahj4hjrcxHuBIQwdG78pWDI97s0HSORltB0ivr5OQUhm3c8pEChFdDJNOMa7aCfdqkt4Vp5sb4tHH8QsxVUuTqVonQujIb86iKA1qlzvz862WfB3i6vgRp+h1OfE=");
//        keyPair.put(RsaUtil.KEY_TYPE_PUBLIC_KEY, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi1VcxqtjBJpcjnvcd5PNG2RFjLmVVPVViRjt/aeLBCqdQ+ek4TQa/7RV+q7jgaMphN1Vbhle3w1PljCuxePv4SIgJwcK2pdVEJv09b5R8kD/rxBW8b5I6DwzVL+qcRFzvRvCRed5/uh3hexPwP5A8JQj+u/JK1TInc/466LWq9Lx6dS0nr6ZeyemhR2jG+rbV5aP8vZAufqBKdU64r5wXX4I8X1DBP2VYgE7JudRw/hFotygjCUSAsiJ3wj66e8D6GmXmhR5j4nNbBF2Rbzxqfq08oL1myZhQujaD1m0/ETHBuA8MfMtsc6DDTqyDYK8cumkVT6uJwrEowuhGYwIjQIDAQAB");

        String str = "Hello world";
        String privateKeyEncryptStr = RsaUtil.rsaEncrypt(keyPair.get(RsaUtil.KEY_TYPE_PRIVATE_KEY), str, RsaUtil.KEY_TYPE_PRIVATE_KEY);
        String publicKeyDecryptStr = RsaUtil.rsaDecrypt(keyPair.get(RsaUtil.KEY_TYPE_PUBLIC_KEY), privateKeyEncryptStr, RsaUtil.KEY_TYPE_PUBLIC_KEY);

        LogUtil.loggerLine(Log.of("Demo", "test33", "privateKeyEncryptStr", privateKeyEncryptStr));
        LogUtil.loggerLine(Log.of("Demo", "test33", "publicKeyDecryptStr", publicKeyDecryptStr));
    }

    private void test34() {
        String content = "Hello world";

        String contentBase64 = Base64.getEncoder().encodeToString(content.getBytes());
        LogUtil.loggerLine(Log.of("Demo", "test34", "contentBase64", contentBase64));
        contentBase64 = Base64.getEncoder().encodeToString(contentBase64.getBytes());
        LogUtil.loggerLine(Log.of("Demo", "test34", "contentBase64", contentBase64));

        String compressedStr = LZString.compressToEncodedURIComponent(contentBase64);
        LogUtil.loggerLine(Log.of("Demo", "test34", "compressedStr", compressedStr));

        String decompressedStr = LZString.decompressFromEncodedURIComponent(compressedStr);
        LogUtil.loggerLine(Log.of("Demo", "test34", "decompressedStr", decompressedStr));

        String message = new String(Base64.getDecoder().decode(decompressedStr));
        LogUtil.loggerLine(Log.of("Demo", "test34", "message", message));

        message = new String(Base64.getDecoder().decode(message));
        LogUtil.loggerLine(Log.of("Demo", "test34", "message", message));
    }

    private void test35() {
        String filePath = "D:\\Document\\MyCodes\\BC-Space\\Worker\\Codes\\service-search\\src\\main\\resources\\bootstrap.yml";
        String regStr = "\\s+active:\\s(\\w+)";
        FileUtil.modContent(filePath, regStr, "test");
    }

    private void test36() {
        String csvPath = "/csv/apply-kind-mapping.csv";
        List<Map<String, String>> lstData = CsvUtil.toMap(csvPath);
        LogUtil.loggerLine(Log.of("Demo", "test36", "lstData.size()", lstData.size()));
    }


    public static JSONObject getAdvertiserInfo() {
        String access_token = "809fa49e8af01b049fe8029c730c15d336e0a4b2";
        final Long advertiser_id = 1667291069364296L;

        // 请求地址
        String open_api_url_prefix = "https://ad.oceanengine.com/open_api/2/";
        String uri = "advertiser/info/";

        // 请求参数
        Map data = new HashMap() {
            {
                put("advertiser_ids", new Long[]{advertiser_id});
                put("fields", new String[]{"id", "name", "status"});
            }
        };

        // 构造请求
        HttpEntityEnclosingRequestBase httpEntity = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "GET";
            }
        };

        httpEntity.setHeader("Access-Token", access_token);

        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;

        try {
            client = HttpClientBuilder.create().build();
            httpEntity.setURI(URI.create(open_api_url_prefix + uri));
            httpEntity.setEntity(new StringEntity(JSONObject.toJSONString(data), ContentType.APPLICATION_JSON));

            response = client.execute(httpEntity);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                bufferedReader.close();
                return JSONObject.parseObject(result.toString());
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void test37() {
        JSONObject jsonObject = getAdvertiserInfo();
        LogUtil.loggerLine(Log.of("Demo", "test37", "jsonObject", jsonObject));
    }

    private void test38() {
        String csvPath = "/csv/max-compute/ocean-engine-callback.csv";
        List<Map<String, String>> lstData = CsvUtil.toMap(csvPath);
        Map<String, Object> mapParams = new LinkedHashMap<>();
        mapParams.put("eventType", "");
        for (Map<String, String> mapData : lstData) {
            String fieldHump = mapData.get("fieldHump");
            String param = mapData.get("param");
            if (StringUtils.hasText(param)) {
                param = String.format("__%s__", param);
            }
            mapParams.put(fieldHump, param);
        }

        String url = "https://test.baochuncare.com/api/warehouse/v1/bc/public/adStore/tt/listener";
        List<String> eventTypes = Arrays.asList("active", "in_app_order", "active_register");
        for (String eventType : eventTypes) {
            mapParams.put("eventType", eventType);

            String tempUrl = ApiUtil.getUrl(url, mapParams);
            LogUtil.loggerLine(Log.of("Demo", "test38", "tempUrl", tempUrl));
            System.out.println("-------------------------------------------------------------------------------------------");
        }
    }

    private void test39() {
//        String csvPath = "D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\max-compute\\ocean-engine-callback.csv";
        String csvPath = "D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\max-compute\\tencent-advertising-click-callback.csv";
        List<Map<String, String>> lstData = CsvUtil.toMap(csvPath);
        List<Map<String, String>> datas = new ArrayList<>();
        for (Map<String, String> mapData : lstData) {
            String param = mapData.get("param");
            String desc = mapData.get("desc");
            String field = mapData.get("field");

            String fieldHump = field.replace("_", "-");
            fieldHump = GenUtil.toHump(fieldHump);
            fieldHump = fieldHump.substring(0, 1).toLowerCase() + fieldHump.substring(1);
            if (StringUtils.hasText(param) && !param.startsWith("__") && !param.endsWith("__")) {
                param = String.format("__%s__", param);
            }

            Map<String, String> tempMapData = new LinkedHashMap<>();
            tempMapData.put("field", field);
            tempMapData.put("fieldHump", fieldHump);
            tempMapData.put("param", param);
            tempMapData.put("desc", desc);
            datas.add(tempMapData);
        }

        CsvUtil.printRecords(csvPath, datas, "field", "fieldHump", "param", "desc");
    }

    public static void run(String[] args) {
        Demo demo = new Demo();
        demo.test39();
//        demo.test38();
//        demo.test37();
//        demo.test36();
//        demo.test35();
//        demo.test34();
//        demo.test33();
//        demo.test32();
//        demo.test31();
//        demo.test30();
//        demo.test29();
//        demo.test28();
//        demo.test27();
//        demo.test26();
//        demo.test25();
//        demo.test24();
//        demo.test23();
//        demo.test22();
//        demo.test21();
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
