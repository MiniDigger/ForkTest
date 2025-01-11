Creating a fork
=====================

## Requirements

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
Which you can do with the following command:
- `./gradlew fixupPaperServerFilePatches` for a per-file patch, stored in `fork-server/paper-patches/files/(the classpath)`

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

## Modifying (per-file) Minecraft patches

This is generally what you need to do when editing Minecraft files. Updating our
per-file patches is as easy as:
1. Making your changes;
2. Running `./gradlew fixupSourcePatches` in the root directory;
3. If nothing went wrong, rebuilding patches with `./gradlew rebuildAllServerPatches`;

## Modifying (per-file) Paper API patches

This is generally what you need to do when you make small changes in the existing API implementation
1. Cd into the `paper-api/src/main/java`
2. Make your changes
3. Run `./gradlew fixupPaperApiFilePatches`
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

#### In order to run `git rebase -i base` you have to be in the java source directory and not in the root server/api dir!
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

## Formatting

All modifications to Vanilla files should be marked. For historical reasons,
API and API-implementation contain a lot of these too, but they are no longer
required, however it's up to your judgment if you want to still keep them as it can make mainteinance easier on your end.

- You need to add a comment with a short and identifiable description of the patch:
  `// Fork start - <COMMIT DESCRIPTION>`
    - The comments should generally be about the reason the change was made, what
      it was before, or what the change is.
    - After the general commit description, you can add additional information either
      after a `;` or in the next line.
- Multi-line changes start with `// Paper Fork - <COMMIT DESCRIPTION>` and end
  with `// Fork end - <COMMIT DESCRIPTION>`.
- One-line changes should have `// Fork - <COMMIT DESCRIPTION>` at the end of the line.

> [!NOTE] These comments are incredibly important to be able to keep track of changes
> across files and to remember what they are for, even a decade into the future.

Here's an example of how to mark changes by Paper:

```java
entity.getWorld().dontBeStupid(); // Fork - Move away from beStupid()
entity.getFriends().forEach(Entity::explode);
entity.updateFriends();

// Fork start - Use plugin-set spawn
// entity.getWorld().explode(entity.getWorld().getSpawn());
Location spawnLocation = ((CraftWorld) entity.getWorld()).getSpawnLocation();
entity.getWorld().explode(new BlockPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ()));
// Fork end - Use plugin-set spawn
```

We generally follow the usual Java style (aka. Oracle style), or what is programmed
into most IDEs and formatters by default. There are a few notes, however:
- It is fine to go over 80 lines as long as it doesn't hurt readability.  
There are exceptions, especially in Spigot-related files
- When in doubt or the code around your change is in a clearly different style,
use the same style as the surrounding code.
- Usage of the `var` keyword is discouraged, as it makes reading patch files a
lot harder and can lead to confusion during updates due to changed return types.
The only exception to this is if a line would otherwise be way too long/filled with
hard to parse generics in a case where the base type itself is already obvious.

### Imports
When adding new imports to a Vanilla class, use the fully qualified class name
instead of adding a new import to the top of the file. If you are using a type a significant number of times, you
can add an import with a comment. However, if it's only used a couple of times, the FQN is preferred to prevent future
patch conflicts in the import section of the file.


```java
import net.minecraft.server.MinecraftServer;
// don't add import here, use FQN like below

public class SomeVanillaClass {
    public final org.bukkit.Location newLocation; // Paper - add location
}
```

### Nullability annotations

We are in the process of switching nullability annotation libraries, so you might need to use one or the other:

**For classes we add**: Fields, method parameters and return types that are nullable should be marked via the
`@Nullable` annotation from `org.jspecify.annotations`. Whenever you create a new class, add `@NullMarked`, meaning types
are assumed to be non-null by default. For less obvious placing such as on generics or arrays, see the [JSpecify docs](https://jspecify.dev/docs/user-guide/).

**For other classes**: Keep using both `@Nullable` and `@NotNull` from `org.jetbrains.annotations`. These
will be replaced later.

## Access Transformers
Sometimes, Vanilla code already contains a field, method, or type you want to access
but the visibility is too low (e.g. a private field in an entity class). Paper and its forks can use access transformers
to change the visibility or remove the final modifier from fields, methods, and classes. Inside the `build-data/fork.at`
file, you can add ATs that are applied when you `./gradlew applyAllPatches`. You can read about the format of ATs 
[here](https://mcforge.readthedocs.io/en/latest/advanced/accesstransformers/#access-modifiers).

<!--
## Patch Notes

When submitting feature patches to Paper, we may ask you to add notes to the patch
header. While we do not require it for all changes, you should add patch notes
when the changes you're making are technical, complex, or require an explanation
of some kind. It is very likely that your patch will remain long after we've all
forgotten about the details of your PR; patch notes will help us maintain it
without having to dig back through GitHub history looking for your PR.

These notes should express the intent of your patch, as well as any pertinent
technical details we should keep in mind long-term. Ultimately, they exist to
make it easier for us to maintain the patch across major version changes.

If you add a message to your commit in the Vanilla source directory,
the rebuild patches script will handle these patch notes
automatically as part of generating the patch file. If you are not
extremely careful, you should always just `squash` or `amend` a patch (see the
above sections on modifying patches) and rebuild.

Editing messages and patches by hand is possible, but you should patch and
rebuild afterwards to make sure you did it correctly. This is slower than just
modifying the patches properly after a few times, so you will not really gain
anything but headaches from doing it by hand.

Underneath is an example patch header/note:

```patch
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Shane Freeder <theboyetronic@gmail.com>
Date: Sun, 15 Oct 2017 00:29:07 +0100
Subject: [PATCH] revert serverside behavior of keepalives

This patch intends to bump up the time that a client has to reply to the
server back to 30 seconds as per pre 1.12.2, which allowed clients
more than enough time to reply potentially allowing them to be less
temperamental due to lag spikes on the network thread, e.g. that caused
by plugins that are interacting with netty.

We also add a system property to allow people to tweak how long the server
will wait for a reply. There is a compromise here between lower and higher
values, lower values will mean that dead connections can be closed sooner,
whereas higher values will make this less sensitive to issues such as spikes
from networking or during connections flood of chunk packets on slower clients,
 at the cost of dead connections being kept open for longer.

diff --git a/src/main/java/net/minecraft/server/PlayerConnection.java b/src/main/java/net/minecraft/server/PlayerConnection.java
index a92bf8967..d0ab87d0f 100644
--- a/src/main/java/net/minecraft/server/PlayerConnection.java
+++ b/src/main/java/net/minecraft/server/PlayerConnection.java
```
-->

## Obfuscation Helpers

While rarely needed, obfuscation helpers are sometimes useful when it comes
to unmapped local variables, or poorly named method parameters. In an effort
to make future updates easier on ourselves, Paper tries to use obfuscation
helpers wherever it makes sense. The purpose of these helpers is to make the
code more readable and maintainable. These helpers should be made easy to
inline by the JVM wherever possible.

An example of an obfuscation helper for a local variable:
```java
double d0 = entity.getX(); final double fromX = d0; // Fork - OBFHELPER
// ...   
this.someMethod(fromX); // Fork
```

While they may not always be done in exactly the same way, the general goal is
always to improve readability and maintainability. Use your best judgment and do
what fits best in your situation.

## Testing API changes

### Using the Paper Test Plugin

The Paper project has a `test-plugin` module for easily testing out API changes
and additions. To use the test plugin, enable it in `test-plugin.settings.gradle.kts`,
which will be generated after running Gradle at least once. After this, you can edit
the test plugin, and run a server with the plugin using `./gradlew runDev` (or any
of the other Paper run tasks).

### Publishing to Maven local (use in external plugins)

To build and install the Paper APIs and Server to your local Maven repository, do the following:

- Run `./gradlew publishToMavenLocal` in the base directory.

If you use Gradle to build your plugin:
- Add `mavenLocal()` as a repository. Gradle checks repositories in the order they are declared,
  so if you also have the Paper repository added, put the local repository above Paper's.
- Make sure to remove `mavenLocal()` when you are done testing, see the [Gradle docs](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:case-for-maven-local)
  for more details.

If you use Maven to build your plugin:
- If you later need to use the Paper-API, you might want to remove the jar
  from your local Maven repository.  
  If you use Windows and don't usually build using WSL, you might not need to
  do this.

## Frequently Asked Questions

### My commit doesn't need a build, what do I do?

Quite simple: You add `[ci skip]` to the start of your commit subject.

This case most often applies to changes to files like `README.md`, this very
file (`CONTRIBUTING.md`), the `LICENSE.md` file, and so forth.

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
