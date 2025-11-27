plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta10"
}

group = "com.xyrisdev"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.xyrisdev.com/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-snapshots")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    compileOnly("org.projectlombok:lombok:1.18.36")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.xyrisdev:XLibrary:1.0.0")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    implementation("net.wesjd:anvilgui:1.10.10-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.xyrisdev.library", "com.xyrisdev.shaded.library")
    relocate("net.wesjd.anvilgui", "com.xyrisdev.shaded.anvilgui")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
