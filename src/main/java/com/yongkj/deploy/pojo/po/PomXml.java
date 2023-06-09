package com.yongkj.deploy.pojo.po;

public class PomXml {

    private static final String EXTERNAL_LIBRARIES = "<plugins>\n" +
            "            <plugin>\n" +
            "                <artifactId>maven-jar-plugin</artifactId>\n" +
            "                <version>3.0.2</version>\n" +
            "                <configuration>\n" +
            "                    <archive>\n" +
            "                        <manifest>\n" +
            "                            <addClasspath>true</addClasspath>\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\n" +
            "                            <classpathPrefix>../libs/</classpathPrefix>\n" +
            "                        </manifest>\n" +
            "                    </archive>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-dependency-plugin</artifactId>-->\n" +
            "<!--                <version>3.0.2</version>-->\n" +
            "<!--                <executions>-->\n" +
            "<!--                    <execution>-->\n" +
            "<!--                        <id>copy-dependencies</id>-->\n" +
            "<!--                        <phase>package</phase>-->\n" +
            "<!--                        <goals>-->\n" +
            "<!--                            <goal>copy-dependencies</goal>-->\n" +
            "<!--                        </goals>-->\n" +
            "<!--                        <configuration>-->\n" +
            "<!--                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>-->\n" +
            "<!--                        </configuration>-->\n" +
            "<!--                    </execution>-->\n" +
            "<!--                </executions>-->\n" +
            "<!--            </plugin>-->\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-assembly-plugin</artifactId>-->\n" +
            "<!--                <version>3.3.0</version>-->\n" +
            "<!--                <configuration>-->\n" +
            "<!--                    <archive>-->\n" +
            "<!--                        <manifest>-->\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\n" +
            "<!--                        </manifest>-->\n" +
            "<!--                    </archive>-->\n" +
            "<!--                    <descriptorRefs>-->\n" +
            "<!--                        <descriptorRef>jar-with-dependencies</descriptorRef>-->\n" +
            "<!--                    </descriptorRefs>-->\n" +
            "<!--                </configuration>-->\n" +
            "<!--                <executions>-->\n" +
            "<!--                    <execution>-->\n" +
            "<!--                        <id>make-assembly</id>-->\n" +
            "<!--                        <phase>package</phase>-->\n" +
            "<!--                        <goals>-->\n" +
            "<!--                            <goal>single</goal>-->\n" +
            "<!--                        </goals>-->\n" +
            "<!--                    </execution>-->\n" +
            "<!--                </executions>-->\n" +
            "<!--            </plugin>-->\n" +
            "        </plugins>";
    private static final String EXTERNAL_LIBRARIES_UPDATE = "<plugins>\n" +
            "            <plugin>\n" +
            "                <artifactId>maven-jar-plugin</artifactId>\n" +
            "                <version>3.0.2</version>\n" +
            "                <configuration>\n" +
            "                    <archive>\n" +
            "                        <manifest>\n" +
            "                            <addClasspath>true</addClasspath>\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\n" +
            "                            <classpathPrefix>../libs/</classpathPrefix>\n" +
            "                        </manifest>\n" +
            "                    </archive>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "            <plugin>\n" +
            "                <artifactId>maven-dependency-plugin</artifactId>\n" +
            "                <version>3.0.2</version>\n" +
            "                <executions>\n" +
            "                    <execution>\n" +
            "                        <id>copy-dependencies</id>\n" +
            "                        <phase>package</phase>\n" +
            "                        <goals>\n" +
            "                            <goal>copy-dependencies</goal>\n" +
            "                        </goals>\n" +
            "                        <configuration>\n" +
            "                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>\n" +
            "                        </configuration>\n" +
            "                    </execution>\n" +
            "                </executions>\n" +
            "            </plugin>\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-assembly-plugin</artifactId>-->\n" +
            "<!--                <version>3.3.0</version>-->\n" +
            "<!--                <configuration>-->\n" +
            "<!--                    <archive>-->\n" +
            "<!--                        <manifest>-->\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\n" +
            "<!--                        </manifest>-->\n" +
            "<!--                    </archive>-->\n" +
            "<!--                    <descriptorRefs>-->\n" +
            "<!--                        <descriptorRef>jar-with-dependencies</descriptorRef>-->\n" +
            "<!--                    </descriptorRefs>-->\n" +
            "<!--                </configuration>-->\n" +
            "<!--                <executions>-->\n" +
            "<!--                    <execution>-->\n" +
            "<!--                        <id>make-assembly</id>-->\n" +
            "<!--                        <phase>package</phase>-->\n" +
            "<!--                        <goals>-->\n" +
            "<!--                            <goal>single</goal>-->\n" +
            "<!--                        </goals>-->\n" +
            "<!--                    </execution>-->\n" +
            "<!--                </executions>-->\n" +
            "<!--            </plugin>-->\n" +
            "        </plugins>";
    private static final String INTERNAL_LIBRARIES = "<plugins>\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-jar-plugin</artifactId>-->\n" +
            "<!--                <version>3.0.2</version>-->\n" +
            "<!--                <configuration>-->\n" +
            "<!--                    <archive>-->\n" +
            "<!--                        <manifest>-->\n" +
            "<!--                            <addClasspath>true</addClasspath>-->\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\n" +
            "<!--                            <classpathPrefix>../libs/</classpathPrefix>-->\n" +
            "<!--                        </manifest>-->\n" +
            "<!--                    </archive>-->\n" +
            "<!--                </configuration>-->\n" +
            "<!--            </plugin>-->\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-dependency-plugin</artifactId>-->\n" +
            "<!--                <version>3.0.2</version>-->\n" +
            "<!--                <executions>-->\n" +
            "<!--                    <execution>-->\n" +
            "<!--                        <id>copy-dependencies</id>-->\n" +
            "<!--                        <phase>package</phase>-->\n" +
            "<!--                        <goals>-->\n" +
            "<!--                            <goal>copy-dependencies</goal>-->\n" +
            "<!--                        </goals>-->\n" +
            "<!--                        <configuration>-->\n" +
            "<!--                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>-->\n" +
            "<!--                        </configuration>-->\n" +
            "<!--                    </execution>-->\n" +
            "<!--                </executions>-->\n" +
            "<!--            </plugin>-->\n" +
            "            <plugin>\n" +
            "                <artifactId>maven-assembly-plugin</artifactId>\n" +
            "                <version>3.3.0</version>\n" +
            "                <configuration>\n" +
            "                    <archive>\n" +
            "                        <manifest>\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\n" +
            "                        </manifest>\n" +
            "                    </archive>\n" +
            "                    <descriptorRefs>\n" +
            "                        <descriptorRef>jar-with-dependencies</descriptorRef>\n" +
            "                    </descriptorRefs>\n" +
            "                </configuration>\n" +
            "                <executions>\n" +
            "                    <execution>\n" +
            "                        <id>make-assembly</id>\n" +
            "                        <phase>package</phase>\n" +
            "                        <goals>\n" +
            "                            <goal>single</goal>\n" +
            "                        </goals>\n" +
            "                    </execution>\n" +
            "                </executions>\n" +
            "            </plugin>\n" +
            "        </plugins>";
    private static final String INTERNAL_LIBRARIES_EXCLUDE = " <plugins>\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-jar-plugin</artifactId>-->\n" +
            "<!--                <version>3.0.2</version>-->\n" +
            "<!--                <configuration>-->\n" +
            "<!--                    <archive>-->\n" +
            "<!--                        <manifest>-->\n" +
            "<!--                            <addClasspath>true</addClasspath>-->\n" +
            "<!--                            <mainClass>com.yongkj.App</mainClass>-->\n" +
            "<!--                            <classpathPrefix>../libs/</classpathPrefix>-->\n" +
            "<!--                        </manifest>-->\n" +
            "<!--                    </archive>-->\n" +
            "<!--                </configuration>-->\n" +
            "<!--            </plugin>-->\n" +
            "<!--            <plugin>-->\n" +
            "<!--                <artifactId>maven-dependency-plugin</artifactId>-->\n" +
            "<!--                <version>3.0.2</version>-->\n" +
            "<!--                <executions>-->\n" +
            "<!--                    <execution>-->\n" +
            "<!--                        <id>copy-dependencies</id>-->\n" +
            "<!--                        <phase>package</phase>-->\n" +
            "<!--                        <goals>-->\n" +
            "<!--                            <goal>copy-dependencies</goal>-->\n" +
            "<!--                        </goals>-->\n" +
            "<!--                        <configuration>-->\n" +
            "<!--                            <outputDirectory>${project.build.directory}/../libs</outputDirectory>-->\n" +
            "<!--                        </configuration>-->\n" +
            "<!--                    </execution>-->\n" +
            "<!--                </executions>-->\n" +
            "<!--            </plugin>-->\n" +
            "            <plugin>\n" +
            "                <artifactId>maven-assembly-plugin</artifactId>\n" +
            "                <version>3.3.0</version>\n" +
            "                <configuration>\n" +
            "                    <archive>\n" +
            "                        <manifest>\n" +
            "                            <mainClass>com.yongkj.App</mainClass>\n" +
            "                        </manifest>\n" +
            "                    </archive>\n" +
            "<!--                    <descriptorRefs>-->\n" +
            "<!--                        <descriptorRef>jar-with-dependencies</descriptorRef>-->\n" +
            "<!--                    </descriptorRefs>-->\n" +
            "                    <descriptors>\n" +
            "                        <descriptor>src/main/resources/package.xml</descriptor>\n" +
            "                    </descriptors>\n" +
            "                </configuration>\n" +
            "                <executions>\n" +
            "                    <execution>\n" +
            "                        <id>make-assembly</id>\n" +
            "                        <phase>package</phase>\n" +
            "                        <goals>\n" +
            "                            <goal>single</goal>\n" +
            "                        </goals>\n" +
            "                    </execution>\n" +
            "                </executions>\n" +
            "            </plugin>\n" +
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
