Paper ForkTest (creating a fork using paperweight)
=====================
This repo aims to be an example fork using paperweight, showcasing the patching system.

The files of most importance are
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
When updating upstream, be sure to keep the dependencies noted in build.gradle.kts in sync with upstream. It's also a good idea to use the same version of the Gradle wrapper and paperweight as upstream.

Below you can find detailed info about the patch system's inner workings (based on a paper fork).

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

Unlike before, changes to the api and server are done through two different directories.
The changes to the original sources are split into the `paper-` directories and the `fork-` directories.

In order to modify code other than minecraft itself (such as paper, spigot sources), you have to make the appropriate changes in the `paper-server` directory.
For example if you were to want to modify Paper's config file, you would have to cd into `paper-server/src/main/java` and make your changes there, following it with rebuilding patches.
Which you can do with the following commands:
- `./gradlew fixupPaperServerFilePatches`
- `./gradlew rebuildPaperServerFilePatches` 
For a per-file patch, stored in `fork-server/paper-patches/files/(the classpath)`

To learn how to make a feature patch, please follow the guide further.

To modify the API you have to make a distinction between API additions and API changes.
The changes to the existing API should be done in the `paper-api/src/main/java` directory and later added in the root `paper-api` dir or in `paper-api/src/main/java` depending on if you want to have a feature patch or a per-file patch.
To rebuild all paper-api patches you can simply run:
- `./gradlew rebuildPaperApiPatches`

Changes to the API such as your own additions should be made in your fork's `fork-api/src/main/java` dir. 
This doesn't require any additional steps such as patching/rebuilding patches.


## Understanding Patches

Unlike adding new API, modifications to Minecraft source files are done through patches.
These patches/extensions are split into different three different sets in two directories depending on where the change was made, which are:
`fork-server/minecraft-patches`
- `sources`: Per-file patches to Minecraft classes;
- `resources`: Per-file patches to Minecraft data files;
- `features`: Larger feature patches that modify multiple Minecraft classes.
-----
`fork-server/paper-patches`
- `files`: Per-file patches to non-minecraft classes such as `io/papermc/paper/PaperConfig`;
- `features`: Larger feature patches that modify multiple non-minecraft classes.

Because this entire structure is based on patches and git, a basic understanding
of how to use git is required. A basic tutorial can be found here:
<https://git-scm.com/docs/gittutorial>.

Assuming you have already forked the repository:

1. Clone your fork to your local machine;
2. Type `./gradlew applyAllPatches` in a terminal to apply the patches.
On Windows, remove the `./` the beginning of `gradlew` commands, unless you are using powershell;
3. cd into `fork-server` or `paper-server` for server changes, and `paper-api` for existing API changes and or `fork-api` for new APIs.
**Only changes made in `fork-server/src/minecraft`, `paper-server/src/main/java` and `paper-api/src/main/java` have to deal with the patch system.**

`fork-server/src/minecraft` is not a git repositories in the traditional sense. Its
initial commits are the decompiled and deobfuscated Minecraft source files. The per-file
patches are applied on top of these files as a single, large commit, which is then followed
by the individual feature-patch commits.

## Modifying the build.gradle.kts files in the server and api dirs

This is done by simply making the appropriate changes and rebuilding the file with:
- `./gradlew rebuildPaperSingleFilePatches`

## Creating and modifying (per-file) Minecraft patches

This is generally what you need to do when editing Minecraft files. 
Making per-file patches is as easy as:
1. Cd into the `fork-server/src/minecraft` dir;
2. Making your changes;
3. Running `./gradlew fixupSourcePatches` in the root directory;
4. If nothing went wrong, rebuilding patches with `./gradlew rebuildMinecraftSourcePatches`;

## Creating and modifying (per-file) Paper-Server patches

This is generally what you need to do when editing `paper-server` files.
Making per-file patches is as easy as:
1. Cd into the `paper-server/src/main/java` dir;
2. Making your changes;
3. Running `./gradlew fixupPaperServerFilePatches` in the root directory;
4. If nothing went wrong, rebuilding patches with `./gradlew rebuildPaperServerFilePatches`;

## Creating and modifying (per-file) Paper API patches

This is generally what you need to do when you make small changes in the existing API.
1. Cd into the `paper-api/src/main/java` dir;
2. Make your changes;
3. Run `./gradlew fixupPaperApiFilePatches`;
4. If nothing went wrong, rebuild patches with `./gradlew rebuildPaperFilePatches`;

### Resolving rebase conflicts
If you run into conflicts while running `fixupSourcePatches` or the `fixupPaperApiFilePatches`, you need to go a more
manual route:

This method works by temporarily resetting your `HEAD` to the desired commit to
edit it using `git rebase`.

0. If you have changes you are working on, type `git stash` to store them for
   later;
    - You can type `git stash pop` to get them back at any point.
1. cd into `fork-server/src/minecraft/java` or `paper-server/src/main/java` or the api dirs and run `git rebase -i base`;
    - It should show something like
      [this](https://gist.github.com/zachbr/21e92993cb99f62ffd7905d7b02f3159) in
      the text editor you get.
    - If your editor does not have a "menu" at the bottom, you're using `vim`.  
      If you don't know how to use `vim` and don't want to
      learn, enter `:q!` and press enter. Before redoing this step, do
      `export EDITOR=nano` for an easier editor to use.
1. Replace `pick` with `edit` for the commit/patch you want to modify (in this
   case the very first commit, `paper File Patches`), and
   "save" the changes;
1. Make the changes you want to make to the patch;
1. Run `git add .` to add your changes;
1. Run `git commit --amend` to commit;
1. Run `git rebase --continue` to finish rebasing;
1. Run `./gradlew rebuildPaperFilePatches` in the root directory;

## Adding larger feature patches

Feature patches are exclusively used for large-scale changes that are hard to
track and maintain and that can be optionally dropped, such as the more involved
optimizations we have. This makes it easier to update the server during Minecraft updates,
since we can temporarily drop these patches and reapply them later.
Please note you can also use them when modifying paper-api for larger api changes.

There is only a very small chance that you will have to use this system, but adding
such patches is very simple:

To create feature patches for the server:
1. Modify `fork-server/src/minecraft` or `paper-server/src/main/java` with the appropriate changes;
1. Run `git add .` inside the root `fork-server` or `paper-server` directory to add your changes;
1. Run `git commit` with the desired patch message;
1. Run `./gradlew rebuildAllServerPatches` in the root directory.

To create feature patches for the api, the process is fairly similar:
1. Modify `paper-api/src/main/java`
1. Run `git add .` inside the root directory of `paper-api` to add your changes;
1. Run `git commit` with the desired patch message;
1. Run `./gradlew rebuildPaperFeaturePatches` in the root directory.

Your commit will be converted into a patch.

> ❗ Please note that if you have some specific implementation detail you'd like
> to document, you should do so in the patch message *or* in comments.

## Modifying larger feature patches

One way of modifying feature patches is to reset to the patch commit and follow
the instructions from the [rebase section](#resolving-rebase-conflicts). If you
are sure there won't be any conflicts from later patches, you can also use the
fixup method.

### Fixup method

#### Manual method

**In order to run `git rebase -i base` you have to be in the java source directory and not in the root server/api dir!**
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
1. Run `./gradlew rebuildAllServerPatches` in the root directory, for api replace with `./gradlew rebuildPaperApiPatches`. This will modify the
   appropriate patches based on your commits.


## Tasks list

#### General tasks
- `applyAllPatches` - Applies all patches
- `applyPaperPatches` - Applies all paperApi, paperApiGenerator, paper single file patches
- `applyPaperSingleFilePatches` - Applies all paper single-file patches
- `applyPaperFilePatches` - Applies all paperApi, paperApiGenerator, paper per-file patches
- `applyPaperFeaturePatches` - Applies all paperApi, paperApiGenerator, paper feature patches

#### Server tasks
*Rebuilding*
- `rebuildAllServerPatches` - rebuilds all patches (both paper and minecraft)
- `rebuildPaperServerPatches` - to only rebuild patches made to the `paper-server` source set
- `rebuildPaperServerFeaturePatches` - the same as above but only feature patches
- `rebuildPaperServerFilePatches` - the same just for per-file changes
- `rebuildMinecraftFeaturePatches` - rebuilds all minecraft feature patches
- `rebuildMinecraftSourcePatches` - rebuilds all minecraft source patches
- `rebuildAllServerFeaturePatches` - this is used to rebuild all feature patches 
- `rebuildAllServerFilePatches` - used to rebuild all per-file patches but not feature patches

*Applying*
- `applyAllServerPatches` - applies all patches (both paper and minecraft)
- `applyPaperServerPatches` - to only apply patches made to the `paper-server` source set
- `applyPaperServerFeaturePatches` - the same as above but only feature patches
- `applyPaperServerFilePatches` - the same just for per-file changes
- `applyMinecraftFeaturePatches` - applies all minecraft feature patches
- `applyMinecraftSourcePatches` - applies all minecraft source patches
- `applyAllServerFeaturePatches` - this is used to apply all feature patches 
- `applyAllServerFilePatches` - used to apply all per-file patches but not feature patches

*Making per-file patches*
- `fixupMinecraftSourcePatches` - for making per-file patches for minecraft sources
- `fixupPaperServerFilePatches` - for making per-file patches for the paper source dir

#### API tasks
*Rebuilding*
- `rebuildPaperApiPatches` - rebuilds all patches made to `paper-api`

*Applying*
- `applyPaperApiPatches` - applies all patches made to `paper-api`
- `applyPaperFeaturePatches` - the same as above but only feature patches
- `applyPaperFilePatches` - the same just for per-file changes

*Making per-file patches*
- `fixupPaperFilePatches` - for making per-file patches for the paper source dir

#### API Generator
*Rebuilding* 
- `rebuildPaperApiGeneratorFeaturePatches` - rebuilds feature patches for `paper-api-generator`
- `rebuildPaperApiGeneratorFilePatches` - rebuilds per-file patches for `paper-api-generator`

*Applying*
- `applyPaperApiGeneratorFeaturePatches` - applies feature patches to `paper-api-generator`
- `applyPaperApiGeneratorFilePatches` - applies per-file patches to `paper-api-generator` 

*Making per-file patches*
- `fixupPaperApiGeneratorFilePatches` - for making per-file patches to the api generator

**For a more thorough tasks list use `./gradlew tasks`**

## Access Transformers
Sometimes, Vanilla code already contains a field, method, or type you want to access
but the visibility is too low (e.g. a private field in an entity class). Paper and its forks can use access transformers
to change the visibility or remove the final modifier from fields, methods, and classes. Inside the `build-data/fork.at`
file, you can add ATs that are applied when you `./gradlew applyAllPatches`. You can read about the format of ATs 
[here](https://mcforge.readthedocs.io/en/latest/advanced/accesstransformers/#access-modifiers).

## Frequently Asked Questions

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
