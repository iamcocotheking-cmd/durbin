plugins {
	id("net.fabricmc.fabric-loom-remap")
}

val minecraftVersion = "1.21.1"
val mappingsBuild = "9"
val fapi = "0.116.9"
group = project.property("maven_group") as String
version = "${project.property("version")}+$minecraftVersion"
base.archivesName = "AxolotlClient"

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient.classtweaker"))
	mods {
		create("axolotlclient") {
			sourceSet("main")
		}
		create("axolotlclient-test") {
			sourceSet("test")
		}
	}
	uncompressNestedJars = true
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("org.quiltmc:quilt-mappings:$minecraftVersion+build.$mappingsBuild:intermediary-v2")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:$fapi+$minecraftVersion")

	modImplementation("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+$minecraftVersion")
	include("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+$minecraftVersion")
	modImplementation(include("io.github.axolotlclient:AxolotlClient-config-rounded:${project.property("config")}+$minecraftVersion")!!)

	modCompileOnlyApi("com.terraformersmc:modmenu:8.0.0") {
		exclude(group = "net.fabricmc")
	}

	api(include(project(path = ":common", configuration = "shadow"))!!)

	modCompileOnly("maven.modrinth:world-host:0.5.0+1.21.1-fabric")
	//implementation("org.quiltmc.parsers:json:0.3.0")
	//implementation("org.semver4j:semver4j:5.3.0")

	val noxesiumVersion = "2.3.3"
	modCompileOnly("maven.modrinth:noxesium:$noxesiumVersion")
	//modImplementation("com.noxcrew.noxesium:api:$noxesiumVersion")
	//localRuntime("org.khelekore:prtree:1.5")

	modCompileOnly("maven.modrinth:e4mc:6.0.6-fabric")

	implementation("net.hypixel:mod-api:1.0.1")
	include(modImplementation("maven.modrinth:hypixel-mod-api:1.0.1+build.1+mc1.21")!!)
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class).configureEach {
	// Ensure that the encoding is set to UTF-8, no matter what the system default is
	// this fixes some edge cases with special characters not displaying correctly
	// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_22)) {
		options.release = 21
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
	jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString().contains("alpha")) "snapshots" else "releases"
			url = uri("https://maven.axolotlclient.com/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

publishMods {
	file.set(tasks.remapJar.flatMap { it.archiveFile })
	additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
	changelog.set(rootProject.layout.buildDirectory.file("changelog").map { it.asFile.readText() })
	type.set(STABLE)
	modLoaders.add("fabric")


	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("p2rxzX0q")
		minecraftVersions.set(listOf(minecraftVersion))
		requires { slug = "fabric-api" }
		embeds { slug = "axolotlclient-rendering" }
	}

	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		minecraftVersions.set(listOf(minecraftVersion))
		projectId.set("809392")
		requires { slug = "fabric-api" }
		clientRequired = true
	}
}

tasks.getByName("publishModrinth") {
	dependsOn(rootProject.tasks.getByName("generateVersionChangelog"))
}
tasks.getByName("publishCurseforge") {
	dependsOn(rootProject.tasks.getByName("generateVersionChangelog"))
}
