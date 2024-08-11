# Magnificient Scala Dependency Converter

### Building

Requires scala-cli (duh) and Liberica NIK 24.0.2.r22-nik 

1. Install Liberica NIK 24.0.2.r22-nik via sdkman in this directory:
```bash
sdk env install
```

2. Generate native-image config:
  
```bash
scala-cli run . -J -agentlib:native-image-agent=config-output-dir=(pwd)/resources/META-INF/native-image
```

3. Build assembly jar:

```bash
scala-cli package -f . --assembly -o dep.jar
```

4. Build native image:

```bash
native-image -jar dep.jar \
  -Djava.awt.headless=false \
  -H:ReflectionConfigurationFiles=native-image/reflect-config.json \
  -H:ResourceConfigurationFiles=native-image/resource-config.json \
  --no-fallback \
  -o dep
```

or just run build.sh!