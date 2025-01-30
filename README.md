# AdvancedReplay (Folia Fork)

AdvancedReplay is a Minecraft 1.8 & 1.21 Replay plugin. It can record players on your Server and save the recorded data to a file or database, so you can watch the replays at any time. Currently it records almost every action a player does and can be easily controlled with commands or the API.

**It has serious problems, please don't use it!!!**

## Downloads
### Original version
**Modrinth**: https://modrinth.com/plugin/advancedreplay  
**Spigot**: https://www.spigotmc.org/resources/advancedreplay-1-8-1-21.52849/
### Folia version
need: Minecraft 1.20.6+, Java 21+

**Github**: https://github.com/404Setup/AdvancedReplay-Folia/releases

## API
### Maven
#### Add the repositories:
```xml


<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<repository>
    <id>maven-snapshots</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```
#### Add the dependency:
```xml
<dependency>
    <groupId>com.github.Jumper251</groupId>
    <artifactId>AdvancedReplay</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```
### Gradle
```text
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Jumper251:AdvancedReplay:VERSION'
}
```

### API usage
Some examples on how to use the API can be found on the plugins Spigot page.
