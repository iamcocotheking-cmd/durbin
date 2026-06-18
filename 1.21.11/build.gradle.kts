@file:Suppress("UnstableApiUsage")

plugins {
	id("net.fabricmc.fabric-loom-remap")
}

val minecraftVersion = "1.21.11"
val minecraftFriendly = "1.21.11"
val modmenu = "17.0.0"
val fapi = "0.141.4+1.21.11"
group = project.property("maven_group") as String
version = "${project.property("version")}+$minecraftFriendly"
base.archivesName = "DurbinClient-AxolotlFork"

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
	decompilerOptions.named("vineflower") {
		options.put("mark-corresponding-synthetics", "1")
	}
}

repositories {
	maven("https://maven.noxcrew.com/public")
	maven("https://maven.enginehub.org/repo/")
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(loom.layered {
		officialMojangMappings {
			nameSyntheticMembers = true
		}
		parchment("org.parchmentmc.data:parchment-1.21.11:2025.12.20@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:$fapi")

	val configVersion = "3.1.12"
	modImplementation("io.github.axolotlclient:AxolotlClient-config:$configVersion+$minecraftFriendly")
	include("io.github.axolotlclient:AxolotlClient-config:$configVersion+$minecraftFriendly")
	modImplementation(include("io.github.axolotlclient:AxolotlClient-config-rounded:$configVersion+$minecraftFriendly")!!)

	modCompileOnly("com.terraformersmc:modmenu:$modmenu")

	api(include(project(path = ":common", configuration = "shadow"))!!)

	modCompileOnly("maven.modrinth:world-host:0.5.0+1.21.3-fabric")
	//implementation("org.quiltmc.parsers:json:0.3.0")
	//implementation("org.semver4j:semver4j:5.3.0")

	val noxesiumVersion = "3.0.0"
	modCompileOnly("maven.modrinth:noxesium:$noxesiumVersion")
	implementation("com.noxcrew.noxesium:api:$noxesiumVersion")
	//localRuntime("org.khelekore:prtree:1.5")

	compileOnly("maven.modrinth:e4mc:6.0.6-fabric")

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

afterEvaluate {
	tasks.getByName("publishModrinth") {
		dependsOn(rootProject.tasks.getByName("generateVersionChangelog"))
	}
	tasks.getByName("publishCurseforge") {
		dependsOn(rootProject.tasks.getByName("generateVersionChangelog"))
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
