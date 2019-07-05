#!/bin/bash

echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<settings
  xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"
  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
  xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">

 <servers>
        <server>
            <id>distil-maven-snapshot</id>
            <username>"${MAVEN_USERNAME}"</username>
            <password>"${MAVEN_PASSWORD}"</password>
        </server>
        <server>
            <id>distil-maven-release</id>
            <username>"${MAVEN_USERNAME}"</username>
            <password>"${MAVEN_PASSWORD}"</password>
        </server>
</servers>
</settings>" > ./.mvn-settings.xml
