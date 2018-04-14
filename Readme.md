<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.iaas.progs</groupId>
  <artifactId>web</artifactId>
  <version>1</version>
  <packaging>war</packaging>

  <name>web</name>
  <url>http://maven.apache.org</url>

  <dependencies>
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>servlet-api</artifactId>
      <version>2.4</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>javax.servlet.jsp</groupId>
      <artifactId>jsp-api</artifactId>
      <version>2.1</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>2.0.2</version>
        <configuration>
          <source>1.4</source>
          <target>1.4</target>
        </configuration>
      </plugin>
	  
	  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.9</version>
    <configuration>
     <skip>false</skip>
    </configuration>
    <executions>
     <execution>
      <id>surefire-it</id>
      <phase>integration-test</phase>
      <goals>
       <goal>test</goal>
      </goals>
      <configuration>
       <skip>false</skip>
      </configuration>
     </execution>
    </executions>
   </plugin>
   
    </plugins>
  </build>
</project>
