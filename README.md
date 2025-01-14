Forky ForkTest (creating a fork of a fork using paperweight)
=====================
This repo aims to be an example fork of a fork using paperweight, showcasing the patching system.

The files of most importance are
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `forky-server/build.gradle.kts.patch`
- `forky-api/build.gradle.kts.patch`
> [!TIP]
> The `build.gradle.kts.patch` files are important because their changes allow for proper patching and compilation. It's important to patch them correctly as minor issues can cause patching errors!

When updating upstream, be sure to keep the dependencies noted in build.gradle.kts in sync with upstream. It's also a good idea to use the same version of the Gradle wrapper and paperweight as upstream.

Below you can find detailed info about the patch system's inner workings (based on a fork of a fork).

**The guide will use two terms to differentiate between upstream and your fork; namely:**

- `fork` for the upstream fork;
- `forky` for your fork.

For info on how to upgrade forks see [Updating forks from 1.20.3 to hardfork](#updating-forks-from-1203-to-hardfork).

-----
## Requirements to get started

To get started with making changes, you'll need the following software, most of
which can be obtained in (most) package managers such as `apt` (Debian / Ubuntu;
you will most likely use this for WSL), `homebrew` (macOS / Linux), and more:

- `git` (package `git` everywhere);
- A Java 21 or later JDK (packages vary, use Google/DuckDuckGo/etc.).
  - [Adoptium](https://adoptium.net/) has builds for most operating systems.
  - Paper requires JDK 21 to build, however, makes use of Gradle's
    [Toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
    feature to allow building with only JRE 11 or later installed. (Gradle will
    automatically provision JDK 21 for compilation if it cannot find an existing
    install).

If you're on Windows, check
[the section on WSL](#patching-and-building-is-really-slow-what-can-i-do).

If you're compiling with Docker, you can use Adoptium's
[`eclipse-temurin`](https://hub.docker.com/_/eclipse-temurin/) images like so:

```console
# docker run -it -v "$(pwd)":/data --rm eclipse-temurin:21.0.5_11-jdk bash
Pulling image...

root@abcdefg1234:/# javac -version
javac 21.0.5
```

## Understanding the source sets

Unlike before, changes to the api and server are done through three different directories.
The changes to the original sources are split into the `paper-`, the `fork-` and `forky-` directories.

In order to modify code other than minecraft itself (such as paper, spigot sources), you have to make the appropriate changes in the `paper-server` directory.
For example if you were to want to modify Paper's config file, you would have to cd into `paper-server/src/main/java` and make your changes there, following it with rebuilding patches.
Which you can do with the following commands:
- `./gradlew fixupPaperServerFilePatches`
- `./gradlew rebuildPaperServerFilePatches`
  
For a per-file patch, stored in `forky-server/paper-patches/files/(the classpath)`

In order to modify upstream code `fork` code, you have to make the appropriate changes in the `Fork-` directories.
The same way as above, except replacing `PaperServer` with `ForkServer` in the tasks
Modifying the `fork-` code will generate a per-file patch, stored in `forky-server/fork-patches/files/(the classpath)`

To make a feature patch you simply `git add .` in the correct dir `(java, resources)`, commit using `git commit` and rebuild the patches.
For more info please read the guide further

To modify the API you have to make a distinction between API additions and API changes.
The changes to the existing API should be done in the `paper-api/src/main/java` directory and later added in `paper-api/src/main/java` or in the `fork-api/src/main/java` directory and depending on if you want to have a feature patch or a per-file patch, use the appropriate methods.
To rebuild all `(fork, paper)`-api patches you can simply run:
- `./gradlew rebuildPaperApiPatches` or `./gradlew rebuildforkApiPatches`

Changes to the API such as your own additions should be made in your fork's `fork-api/src/main/java` dir. 
This doesn't require any additional steps such as patching/rebuilding patches.

**The server source sets are seperated into four main types:**
- `forky-server/src/main/java`: This contains your own code, such as your fork's config file. This doesn't touch vanilla code and is empty by default
- `forky-server/src/minecraft`: These are the vanilla/modified by paper sources. This is where you'll most likely make changes.
- `fork-server/src/main/java`: This contains the upstream fork's code, such as its config file.
- `paper-server/src/main/java`: This directory contains all Paper related sources; such as Bukkit (please note there are some exceptions and some paper files appear in the minecraft source)

**The API source sets look as follows:**
- `forky-api/src/main/java`: Your own API code, this directory doesn't contain any code by default
- `fork-api/src/main/java`: Your upstream's own API code. This is where you can make changes to its API, whether that is to include your api in an existing one or adjust something
- `paper-api/src/main/java`: This is where you'll make changes to the existing paper API, whether that is to include your api in an existing one or adjust something

## Understanding Patches

Keep in mind that all file and resource patches are going to be stored in their respective classpath such as:
- `forky-server/minecraft-patches/sources/net/minecraft/server/MinecraftServer.java.patch`
  
Feature patches are going to be stored normally.

#### Server sources
Unlike adding new API, modifications to the existing source files are done through patches.
These patches/extensions are split into different 5 (3+2) different sets in two directories depending on where the change was made, which are:

`forky-server/minecraft-patches`
- `sources`: Per-file patches to Minecraft classes;
- `resources`: Per-file patches to Minecraft data files;
- `features`: Larger feature patches that modify multiple Minecraft classes.

  
`forky-server/paper-patches`
- `files`: Per-file patches to non-minecraft classes such as `io/papermc/paper/PaperConfig`;
- `features`: Larger feature patches that modify multiple non-minecraft classes.

`forky-server/fork-patches`
- `files`: Per-file patches to your upstream's classes such as `com/github/forkdev/fork/forkConfig`;
- `features`: Larger feature patches that modify multiple `fork` classes.


#### API sources
Changes to existing APIs are done through patches, be that per-file or feature ones.
These patches are seperated into two dirs with two different sub-directories for file and feature patches.
The structure looks like this:

`forky-api/paper-patches`
- `files`: Per-file patches to existing API classes, such as Paper api or Bukkit;
- `features`: Larger feature patches that contain multiple changes to existing APIs.

`forky-api/fork-patches`
- `files`: Per-file patches to existing API classes from Fork api;
- `features`: Larger feature patches that contain multiple changes to fork's APIs.
----


Because this entire structure is based on patches and git, a basic understanding
of how to use git is required. A basic tutorial can be found here:
<https://git-scm.com/docs/gittutorial>.


Assuming you have already forked the repository:

1. Clone your fork to your local machine;
2. Type `./gradlew applyAllPatches` in a terminal to apply the patches.
On Windows, remove the `./` the beginning of `gradlew` commands, unless you are using powershell;
3. cd into `fork-server`, `paper-server` or `forky-server` for server changes, and `paper-api` or `fork-api` for existing API changes and or `forky-api` for new APIs.
**Only changes made in `fork-server/src/minecraft`, `paper-server/src/main/(java, resources)`, `paper-api/src/main/java` and `fork-api/src/main/java` have to deal with the patch system.**

`fork-server/src/minecraft` is not a git repositories in the traditional sense. Its
initial commits are the decompiled and deobfuscated Minecraft source files. The per-file
patches are applied on top of these files as a single, large commit, which is then followed
by the individual feature-patch commits. 

## Modifying the build.gradle.kts files in the server and api dirs

This is done by simply making the appropriate changes and rebuilding the file with:
- `./gradlew rebuildForkSingleFilePatches`

## Creating and modifying (per-file) Minecraft `java` patches 

This is generally what you need to do when editing Minecraft files. 
To make per-file patches you simply need to:
1. Cd into the `forky-server/src/minecraft/java` dir;
2. Make your changes;
3. Run `./gradlew fixupMinecraftSourcePatches` in the root directory;
4. If nothing went wrong, rebuild the patches with `./gradlew rebuildMinecraftSourcePatches`;

## Creating and modifying (per-file) Minecraft `resources` patches

This is generally what you need to do when editing Minecraft resource patches.
To make per-file patches you simply need to:
1. Cd into the `forky-server/src/minecraft/resources` dir;
2. Make your changes;
3. Run `./gradlew fixupMinecraftResourcePatches` in the root directory;
4. If nothing went wrong, rebuild patches with `./gradlew rebuildMinecraftResourcePatches`;

## Creating and modifying (per-file) Paper `resources` patches

This is generally what you need to do when editing Paper resource patches.
Making per-file patches for Paper resources is a bit more complicated since you need to use `git rebase`:
1. Cd into the `paper-server/src/main/resources` dir;
2. Run `git rebase -i base` and select the first commit; for more info refer to [here](#resolving-rebase-conflicts-manual-per-file-patch-method)
3. Make your changes;
4. Run `git add .` and then commit using `git commit --amend`
5. After you amend the commit, run `git rebase --continue`; If no errors occur you can proceed further, otherwise refer to [here](#resolving-rebase-conflicts-manual-per-file-patch-method)
6. If nothing went wrong, rebuild the patches with `./gradlew rebuildPaperServerPatches` in the root directory;

## Creating and modifying (per-file) Paper-Server or Forky-Server patches

This is generally what you need to do when editing `paper-server` files.
To make per-file patches you simply need to:
1. Cd into the `paper-server/src/main/java` dir;
2. Make your changes;
3. Run `./gradlew fixupPaperServerFilePatches` in the root directory;
4. If nothing went wrong, rebuild the patches with `./gradlew rebuildPaperServerFilePatches`;

**The same applies to `Fork-Server`, except replacing `PaperServer` with `ForkServer` in the tasks and dirs**

## Creating and modifying (per-file) Paper API or Forky API patches

This is generally what you need to do when you make changes in the existing API.
To make per-file patches you simply need to:
1. Cd into the `paper-api/src/main/java` dir;
2. Make your changes;
3. Run `./gradlew fixupPaperApiFilePatches`;
4. If nothing went wrong, rebuild patches with `./gradlew rebuildPaperFilePatches`; **(this command also rebuilds the server file patches!)**

**The same applies to `Fork-API`, except replacing `PaperApi` with `ForkApi` in the tasks and dirs, and replacing the rebuild task to:**
- `./gradlew rebuildForkApiFilePatches`


### Resolving rebase conflicts (manual per-file patch method)
If you run into conflicts while running `fixupMinecraftSourcePatches`, `fixupMinecraftResourcePatches` or the `fixupPaperApiFilePatches`, `fixupForkApiFilePatches`, you need to go a more
manual route:

If you encounter an issue while running git rebase, refer to [here](#i-tried-to-manually-modify-per-file-patches-but-i-cant-find-the-commit-I-only-have-a-noop-commit)

This method works by temporarily resetting your `HEAD` to the desired commit to
edit it using `git rebase`.

0. If you have changes you are working on, type `git stash` to store them for
   later;
    - You can type `git stash pop` to get them back at any point.
1. cd into `fork-server/src/minecraft/(java, resources)` or `paper-server/src/main/(java, resources)` or the `paper-api/src/main/java` dir and run `git rebase -i base`;
    - It should show something like
      [this](https://gist.github.com/zachbr/21e92993cb99f62ffd7905d7b02f3159) in
      the text editor you get.
    - If your editor does not have a "menu" at the bottom, you're using `vim`.  
      If you don't know how to use `vim` and don't want to
      learn, enter `:q!` and press enter. Before redoing this step, do
      `export EDITOR=nano` for an easier editor to use.
1. Replace `pick` with `edit` for the commit/patch you want to modify (in this
   case the very first commit, `fork File Patches`), and
   "save" the changes;
1. Make the changes you want to make to the patch;
1. Run `git add .` to add your changes;
1. Run `git commit --amend` to commit;
1. Run `git rebase --continue` to finish rebasing;
1. Run `./gradlew rebuildAllServerPatches` in the root directory;

## Adding larger feature patches

Feature patches are exclusively used for large-scale changes that are hard to
track and maintain and that can be optionally dropped, such as the more involved
optimizations we have. This makes it easier to update the server during Minecraft updates,
since we can temporarily drop these patches and reapply them later.

**Please note you can also use them when modifying paper-api for larger api changes.**
**You can't use feature patches for minecraft `resources`, however they can be used for `paper-server/src/main/resources`**

There is only a very small chance that you will have to use this system, but adding
such patches is very simple:

To create feature patches for the server:
1. Modify `forky-server/src/minecraft/java` or `(paper-server, fork-server)/src/main/java` with the appropriate changes;
1. Run `git add .` inside the `java` dir to add your changes;
1. Run `git commit` with the desired patch message;
1. Run `./gradlew rebuildAllServerPatches` in the root directory.

To create feature patches for the api, the process is fairly similar:
1. Modify `(paper-api, fork-api)/src/main/java`
1. Run `git add .` inside the `java` dir to add your changes;
1. Run `git commit` with the desired patch message;
1. Run `./gradlew rebuildPaperFeaturePatches` or `./gradlew rebuildForkFeaturePatches` if you modified `forky-api` in the root directory.

Your commit will then be converted into a feature patch, stored in `(forky-api,forky-server)/(paper-patches,minecraft-patches,fork-patches)/features`.

> ❗ Please note that if you have some specific implementation detail you'd like
> to document, you should do so in the patch message *or* in comments.

## Modifying larger feature patches

If you encounter an issue while running git rebase, refer to [here](#i-tried-to-manually-modify-per-file-patches-but-i-cant-find-the-commit-I-only-have-a-noop-commit)

One way of modifying feature patches is to reset to the patch commit and follow
the instructions from the [rebase section](#resolving-rebase-conflicts). If you
are sure there won't be any conflicts from later patches, you can also use the
fixup method.

### Fixup method

#### Manual method

1. Make your changes;
1. Make a temporary commit. You don't need to make a message for this;
1. Type `git rebase -i base`, move (cut) your temporary commit and
   move it under the line of the patch you wish to modify;
1. Change the `pick` to the appropriate action:
    1. `f`/`fixup`: Merge your changes into the patch without touching the
       message.
    1. `s`/`squash`: Merge your changes into the patch and use your commit message
       and subject.
1. Run `./gradlew rebuildAllServerPatches` in the root directory, for api replace with `./gradlew rebuildPaperApiPatches`;
    - This will modify the appropriate patches based on your commits.

#### Automatic method

1. Make your changes;
1. Make a fixup commit: `git commit -a --fixup <hash of patch to fix>`;
    - If you want to modify a per-file patch, use `git commit -a --fixup file`
    - You can also use `--squash` instead of `--fixup` if you want the commit
      message to also be changed.
    - You can get the hash by looking at `git log` or `git blame`; your IDE can
      assist you too.
    - Alternatively, if you only know the name of the patch, you can do
      `git commit -a --fixup "Subject of Patch name"`.
1. Rebase with autosquash: `git rebase -i --autosquash base`.
   This will automatically move your fixup commit to the right place, and you just
   need to "save" the changes.
1. Run `./gradlew rebuildAllServerPatches` in the root directory, for api replace with `./gradlew rebuildPaperApiPatches` or `./gradlew rebuildforkApiPatches` depending on which source you modified. This will modify the
   appropriate patches based on your commits.


## Tasks list
> [!CAUTION]
> Reobf jars are unsupported and are not recommended unless in very specific settings, you should not use the tasks containing `reobf` in their name.

#### General tasks
- `applyAllPatches` - Applies all patches
- `applyPaperPatches` - Applies all paperApi, paperApiGenerator, paper single file patches
- `applyPaperSingleFilePatches` - Applies all paper single-file patches
- `applyPaperFilePatches` - Applies all paperApi, paperApiGenerator, paper per-file patches
- `applyPaperFeaturePatches` - Applies all paperApi, paperApiGenerator, paper feature patches
- `applyForkPatches` - Applies all forkApi, forkApiGenerator, fork single file patches
- `applyForkSingleFilePatches` - Applies all fork single-file patches
- `applyForkFilePatches` - Applies all forkApi, forkApiGenerator, fork per-file patches
- `applyForkFeaturePatches` - Applies all forkApi, forkApiGenerator, fork feature patches
- `applyMinecraftPatches` - Applies all Minecraft patches
- `applyMinecraftResourcePatches` - Applies file patches to the Minecraft resources

#### Running tasks
- `runBundler` - Spins up a test server from the Mojang mapped bundler jar
- `runDevServer` - Spins up a test server without assembling a jar
- `runPaperclip` - Spins up a test server from the Mojang mapped Paperclip jar
- `runServer` - Spins up a test server from the Mojang mapped server jar
- `runReobfBundler` - Spins up a test server from the reobf bundler jar
- `runReobfPaperclip` - Spins up a test server from the reobf Paperclip jar
- `runReobfServer` - Spins up a test server from the reobf bundler jar
- `runReobfServer` - Spins up a test server from the reobfJar output jar

#### Bundling tasks
- `createMojmapBundlerJar` - Builds a runnable bundler jar
- `createMojmapPaperclipJar` - Builds a runnable paperclip jar
- `createReobfBundlerJar` - Builds a runnable reobf bundler jar
- `createReobfPaperclipJar` - Builds a runnable reobf paperclip jar

#### Server tasks
*Rebuilding*
- `rebuildAllServerPatches` - rebuilds all patches (both paper and minecraft)
- `rebuildPaperServerPatches` - to only rebuild patches made to the `paper-server` source set
- `rebuildPaperServerFeaturePatches` - the same as above but only feature patches
- `rebuildPaperServerFilePatches` - the same just for per-file changes
- `rebuildForkServerPatches` - to only rebuild patches made to the `fork-server` source set
- `rebuildForkServerFeaturePatches` - the same as above but only feat
- `rebuildForkServerFilePatches` - the same just for per-file changes
- `rebuildForkMinecraftFilePatches` - rebuilds all fork Minecraft file patches
- `rebuildForkMinecraftPatches` - rebuilds all fork Minecraft patches
- `rebuildForkMinecraftResourcePatches` - rebuilds fork file patches to the Minecraft resources
- `rebuildForkMinecraftSourcePatches` - rebuilds fork file patches to the Minecraft sources
- `rebuildMinecraftFeaturePatches` - rebuilds all minecraft feature patches
- `rebuildMinecraftSourcePatches` - rebuilds all minecraft source patches
- `rebuildMinecraftResourcePatches` - rebuilds all minecraft resource patches
- `rebuildAllServerFeaturePatches` - this is used to rebuild all feature patches 
- `rebuildAllServerFilePatches` - used to rebuild all per-file patches but not feature patches

*Applying*
- `applyAllServerPatches` - applies all patches (both paper and minecraft)
- `applyPaperServerPatches` - to only apply patches made to the `paper-server` source set
- `applyPaperServerFeaturePatches` - the same as above but only feature patches
- `applyPaperServerFilePatches` - the same just for per-file changes
- `applyForkServerPatches` - to only apply patches made to the `fork-server` source set
- `applyForkServerFeaturePatches` - the same as above but only feat
- `applyForkServerFilePatches` - the same just for per-file changes
- `applyForkMinecraftFilePatches` - Applies all fork Minecraft file patches
- `applyForkMinecraftPatches` - Applies all fork Minecraft patches
- `applyForkMinecraftResourcePatches` - Applies fork file patches to the Minecraft resources
- `applyForkMinecraftSourcePatches` - Applies fork file patches to the Minecraft sources
- `applyForkPaperServerFeaturePatches` - Applies paperServer fork feature patches
- `applyForkPaperServerFilePatches` - Applies paperServer fork file patches
- `applyForkPaperServerFilePatchesFuzzy` - Applies paperServer fork file patches
- `applyForkPaperServerPatches` - Applies all paperServer fork patches
- `applyMinecraftFeaturePatches` - applies all minecraft feature patches
- `applyMinecraftSourcePatches` - applies all minecraft source patches
- `applyMinecraftResourcePatches` - applies all minecraft resource patches
- `applyAllServerFeaturePatches` - this is used to apply all feature patches 
- `applyAllServerFilePatches` - used to apply all per-file patches but not feature patches


*Making per-file patches*
- `fixupMinecraftSourcePatches` - for making per-file patches to minecraft `java` source
- `fixupPaperServerFilePatches` - for making per-file patches to the paper source dir
- `fixupForkServerFilePatches` - for making per-file patches to the fork source dir
- `fixupMinecraftResourcePatches` - for making per-file patches to minecraft `resources` source

#### API tasks
*Rebuilding*
- `rebuildPaperApiPatches` - rebuilds all patches made to `paper-api`
- `rebuildForkApiPatches` - rebuilds all patches made to `fork-api`

*Applying*
- `applyPaperApiPatches` - applies all patches made to `paper-api`
- `applyPaperFeaturePatches` - the same as above but only feature patches
- `applyPaperFilePatches` - the same just for per-file changes
- `applyForkApiPatches` - applies all patches made to `fork-api`
- `applyForkFeaturePatches` - the same as above but only feature patches
- `applyForkFilePatches` - the same just for per-file changes

*Making per-file patches*
- `fixupPaperFilePatches` - for making per-file patches for the paper source dir
- `fixupForkFilePatches` - for making per-file patches for the fork source dir

#### API Generator
*Rebuilding* 
- `rebuildPaperApiGeneratorFeaturePatches` - rebuilds feature patches for `paper-api-generator`
- `rebuildPaperApiGeneratorFilePatches` - rebuilds per-file patches for `paper-api-generator``

*Applying*
- `applyPaperApiGeneratorFeaturePatches` - applies feature patches to `paper-api-generator`
- `applyPaperApiGeneratorFilePatches` - applies per-file patches to `paper-api-generator`

*Making per-file patches*
- `fixupPaperApiGeneratorFilePatches` - for making per-file patches to the paper api generator

**For a more thorough tasks list use `./gradlew tasks`**

## Access Transformers
Sometimes, Vanilla code already contains a field, method, or type you want to access
but the visibility is too low (e.g. a private field in an entity class). Paper and its forks can use access transformers
to change the visibility or remove the final modifier from fields, methods, and classes. Inside the `build-data/fork.at`
file, you can add ATs that are applied when you `./gradlew applyAllPatches`. You can read about the format of ATs 
[here](https://mcforge.readthedocs.io/en/latest/advanced/accesstransformers/#access-modifiers).

## Frequently Asked Questions

### Updating forks from 1.20.3 to hardfork

Unfortunately there isn't one single easy way to do this. 
The simplest one would be to try and move all current patches to feature patches in the server dir, modify the build file like shown below, apply (this should generate .rej files) and use the .rej files to manually apply those hunks to the source .java code.

In order to get the .rej files, you need to add the `rejectsFile = file("(set your dir here)")` property in the root `build.gradle.kts`

The file can look something like this:
```gradle
        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("fork-server/build.gradle.kts")
            patchFile = file("fork-server/build.gradle.kts.patch")
            rejectsFile = file("fork-server/rejects")
        }
```

**Repeat that for every source set!**

Also keep in mind that certain hunks of code need to be moved from those patches into paper feature patches as the source is seperated. Read the guide for more info on the new layout.

### I don't know how to fix my patching errors!

For help with using paperweight, you can join our [discord](https://discord.com/channels/289587909051416579/1078993196924813372)!

### I tried to manually modify per-file patches but i can't find the commit! I only have a noop commit

This probably happened because you were running `git rebase` in the wrong directory (`fork-server` for example)

**In order to run `git rebase -i base` you have to be in the java source (or resources) directory and not in the root server/api dir!**

### How to modify the server logo?

Sadly you can't patch the logo.png with file patches;

You can do that however with feature patches, but there exists a better and simpler way which involves adding your own server logo to your fork and just changing the references to use it instead of the paper one.

### Patching and building is *really* slow, what can I do?

This only applies if you're running Windows. If you're running a prior Windows
release, either update to Windows 10/11 or move to macOS/Linux/BSD.

In order to speed up patching process on Windows, it's recommended you get WSL 2.
This is available in Windows 10 v2004, build 19041 or higher. (You can check
your version by running `winver` in the run window (Windows key + R)). If you're
using an out of date version of Windows 10, update your system with the
[Windows 10 Update Assistant](https://www.microsoft.com/en-us/software-download/windows10) or [Windows 11 Update Assistant](https://www.microsoft.com/en-us/software-download/windows11).

To set up WSL 2, follow the information here:
<https://docs.microsoft.com/en-us/windows/wsl/install>

You will most likely want to use the Ubuntu apps. Once it's set up, install the
required tools with `sudo apt-get update && sudo apt-get install $TOOL_NAMES
-y`. Replace `$TOOL_NAMES` with the packages found in the
[requirements](#requirements). You can now clone the repository and do
everything like usual.

> ❗ Do not use the `/mnt/` directory in WSL! Instead, mount the WSL directories
> in Windows like described here:
> <https://docs.microsoft.com/en-us/windows/wsl/filesystems#view-your-current-directory-in-windows-file-explorer>
