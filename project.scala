//> using publish.organization ma.chinespirit
//> using publish.name dep
//> using publish.version 0.1.0-SNAPSHOT

//> using jvm graalvm-java22:22.0.2

//> using resourceDir resources

//> using scala 3.6.2
//> using options -deprecation

// for parsing
//> using dep com.lihaoyi::fastparse::3.1.1
//> using dep org.scala-lang.modules::scala-xml:2.3.0

// for cli parsing
//> using dep com.github.alexarchambault::case-app:2.1.0-M29

// for search api
//> using dep com.softwaremill.sttp.client4::core:4.0.0-M19
//> using dep com.softwaremill.sttp.client4::jsoniter:4.0.0-M19
//> using dep tech.neander::cue4s:0.0.6
//> using dep com.softwaremill.ox::core::0.5.8
//> using dep io.kevinlee::just-semver:1.1.0
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.33.0
//> using compileOnly.dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.33.0

// for web interface // later, maybe
///> using dep com.softwaremill.sttp.tapir::tapir-jdkhttp-server:1.11.9
///> using dep com.vaadin:open:8.5.0.3

// debug
//> using dep com.lihaoyi::pprint:0.9.0

// tests
//> using test.dep org.scalameta::munit::1.0.3

// use sonatype snapshots
