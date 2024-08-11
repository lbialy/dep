#!/bin/bash

java -version 2>&1 | grep "Liberica-NIK-24.0.2-1" > /dev/null || {
  echo "Please install Liberica-NIK-24.0.2-1 using: 'sdk env install'"
  exit 1
}

echo 'implementation("org.openjfx:javafx:22.0.2")' | \
  scala-cli run . -J -agentlib:native-image-agent=config-output-dir=$(pwd)/resources/META-INF/native-image

scala-cli package -f . --assembly -o dep.jar

native-image -jar dep.jar -Djava.awt.headless=false --no-fallback -o dep

rm dep.jar