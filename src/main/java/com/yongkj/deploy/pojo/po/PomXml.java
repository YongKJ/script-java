package com.yongkj.deploy.pojo.po;

public class PomXml {

    private static final String EXTERNAL_LIBRARIES = "<plugins>\r\n" +
            "            <plugin>\r\n" +
            "                <artifactId>maven-jar-plugin</artifactId>\r\n" +
            "                <version>3.0.2</version>\r\n" +
            "                <configuration>\r\n" +
            "                    <archive>\r\n" +
            "                        <manifest>\r\n" +
            "                            <addClasspath>true</addClasspath>\r\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\r\n" +
            "                            <classpathPrefix>../libs/</classpathPrefix>\r\n" +
            "                        </manifest>\r\n" +
            "                    </archive>\r\n" +
            "                </configuration>\r\n" +
            "            </plugin>\r\n" +
            "<!--            <plugin>-->\r\n" +
            "<!--                <artifactId>maven-dependency-plugin</artifactId>-->\r\n" +
            "<!--                <version>3.0.2</version>-->\r\n" +
            "<!--                <executions>-->\r\n" +
            "<!--                    <execution>-->\r\n" +
            "<!--                        <id>copy-dependencies</id>-->\r\n" +
            "<!--                        <phase>package</phase>-->\r\n" +
            "<!--                        <goals>-->\r\n" +
            "<!--                            <goal>copy-dependencies</goal>-->\r\n" +
            "<!--                        </goals>-->\r\n" +
            "<!--                        <configuration>-->\r\n" +
            "<!--                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>-->\r\n" +
            "<!--                        </configuration>-->\r\n" +
            "<!--                    </execution>-->\r\n" +
            "<!--                </executions>-->\r\n" +
            "<!--            </plugin>-->\r\n" +
            "<!--            <plugin>-->\r\n" +
            "<!--                <artifactId>maven-assembly-plugin</artifactId>-->\r\n" +
            "<!--                <version>3.3.0</version>-->\r\n" +
            "<!--                <configuration>-->\r\n" +
            "<!--                    <archive>-->\r\n" +
            "<!--                        <manifest>-->\r\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\r\n" +
            "<!--                        </manifest>-->\r\n" +
            "<!--                    </archive>-->\r\n" +
            "<!--                    <descriptorRefs>-->\r\n" +
            "<!--                        <descriptorRef>jar-with-dependencies</descriptorRef>-->\r\n" +
            "<!--                    </descriptorRefs>-->\r\n" +
            "<!--                </configuration>-->\r\n" +
            "<!--                <executions>-->\r\n" +
            "<!--                    <execution>-->\r\n" +
            "<!--                        <id>make-assembly</id>-->\r\n" +
            "<!--                        <phase>package</phase>-->\r\n" +
            "<!--                        <goals>-->\r\n" +
            "<!--                            <goal>single</goal>-->\r\n" +
            "<!--                        </goals>-->\r\n" +
            "<!--                    </execution>-->\r\n" +
            "<!--                </executions>-->\r\n" +
            "<!--            </plugin>-->\r\n" +
            "        </plugins>";
    private static final String EXTERNAL_LIBRARIES_UPDATE = "<plugins>\r\n" +
            "            <plugin>\r\n" +
            "                <artifactId>maven-jar-plugin</artifactId>\r\n" +
            "                <version>3.0.2</version>\r\n" +
            "                <configuration>\r\n" +
            "                    <archive>\r\n" +
            "                        <manifest>\r\n" +
            "                            <addClasspath>true</addClasspath>\r\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\r\n" +
            "                            <classpathPrefix>../libs/</classpathPrefix>\r\n" +
            "                        </manifest>\r\n" +
            "                    </archive>\r\n" +
            "                </configuration>\r\n" +
            "            </plugin>\r\n" +
            "            <plugin>\r\n" +
            "                <artifactId>maven-dependency-plugin</artifactId>\r\n" +
            "                <version>3.0.2</version>\r\n" +
            "                <executions>\r\n" +
            "                    <execution>\r\n" +
            "                        <id>copy-dependencies</id>\r\n" +
            "                        <phase>package</phase>\r\n" +
            "                        <goals>\r\n" +
            "                            <goal>copy-dependencies</goal>\r\n" +
            "                        </goals>\r\n" +
            "                        <configuration>\r\n" +
            "                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>\r\n" +
            "                        </configuration>\r\n" +
            "                    </execution>\r\n" +
            "                </executions>\r\n" +
            "            </plugin>\r\n" +
            "<!--            <plugin>-->\r\n" +
            "<!--                <artifactId>maven-assembly-plugin</artifactId>-->\r\n" +
            "<!--                <version>3.3.0</version>-->\r\n" +
            "<!--                <configuration>-->\r\n" +
            "<!--                    <archive>-->\r\n" +
            "<!--                        <manifest>-->\r\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\r\n" +
            "<!--                        </manifest>-->\r\n" +
            "<!--                    </archive>-->\r\n" +
            "<!--                    <descriptorRefs>-->\r\n" +
            "<!--                        <descriptorRef>jar-with-dependencies</descriptorRef>-->\r\n" +
            "<!--                    </descriptorRefs>-->\r\n" +
            "<!--                </configuration>-->\r\n" +
            "<!--                <executions>-->\r\n" +
            "<!--                    <execution>-->\r\n" +
            "<!--                        <id>make-assembly</id>-->\r\n" +
            "<!--                        <phase>package</phase>-->\r\n" +
            "<!--                        <goals>-->\r\n" +
            "<!--                            <goal>single</goal>-->\r\n" +
            "<!--                        </goals>-->\r\n" +
            "<!--                    </execution>-->\r\n" +
            "<!--                </executions>-->\r\n" +
            "<!--            </plugin>-->\r\n" +
            "        </plugins>";
    private static final String INTERNAL_LIBRARIES = "<plugins>\r\n" +
            "<!--            <plugin>-->\r\n" +
            "<!--                <artifactId>maven-jar-plugin</artifactId>-->\r\n" +
            "<!--                <version>3.0.2</version>-->\r\n" +
            "<!--                <configuration>-->\r\n" +
            "<!--                    <archive>-->\r\n" +
            "<!--                        <manifest>-->\r\n" +
            "<!--                            <addClasspath>true</addClasspath>-->\r\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\r\n" +
            "<!--                            <classpathPrefix>../libs/</classpathPrefix>-->\r\n" +
            "<!--                        </manifest>-->\r\n" +
            "<!--                    </archive>-->\r\n" +
            "<!--                </configuration>-->\r\n" +
            "<!--            </plugin>-->\r\n" +
            "<!--            <plugin>-->\r\n" +
            "<!--                <artifactId>maven-dependency-plugin</artifactId>-->\r\n" +
            "<!--                <version>3.0.2</version>-->\r\n" +
            "<!--                <executions>-->\r\n" +
            "<!--                    <execution>-->\r\n" +
            "<!--                        <id>copy-dependencies</id>-->\r\n" +
            "<!--                        <phase>package</phase>-->\r\n" +
            "<!--                        <goals>-->\r\n" +
            "<!--                            <goal>copy-dependencies</goal>-->\r\n" +
            "<!--                        </goals>-->\r\n" +
            "<!--                        <configuration>-->\r\n" +
            "<!--                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>-->\r\n" +
            "<!--                        </configuration>-->\r\n" +
            "<!--                    </execution>-->\r\n" +
            "<!--                </executions>-->\r\n" +
            "<!--            </plugin>-->\r\n" +
            "            <plugin>\r\n" +
            "                <artifactId>maven-assembly-plugin</artifactId>\r\n" +
            "                <version>3.3.0</version>\r\n" +
            "                <configuration>\r\n" +
            "                    <archive>\r\n" +
            "                        <manifest>\r\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\r\n" +
            "                        </manifest>\r\n" +
            "                    </archive>\r\n" +
            "                    <descriptorRefs>\r\n" +
            "                        <descriptorRef>jar-with-dependencies</descriptorRef>\r\n" +
            "                    </descriptorRefs>\r\n" +
            "                </configuration>\r\n" +
            "                <executions>\r\n" +
            "                    <execution>\r\n" +
            "                        <id>make-assembly</id>\r\n" +
            "                        <phase>package</phase>\r\n" +
            "                        <goals>\r\n" +
            "                            <goal>single</goal>\r\n" +
            "                        </goals>\r\n" +
            "                    </execution>\r\n" +
            "                </executions>\r\n" +
            "            </plugin>\r\n" +
            "        </plugins>";

    public static String getExternalLibraries() {
        return EXTERNAL_LIBRARIES;
    }

    public static String getExternalLibrariesUpdate() {
        return EXTERNAL_LIBRARIES_UPDATE;
    }

    public static String getInternalLibraries() {
        return INTERNAL_LIBRARIES;
    }
}
