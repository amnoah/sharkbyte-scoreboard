![Image](/files/SharkByte_Logo.png)

# sharkbyte-scoreboard 2.x.x

This is a platform-independent system that allows scoreboards to be created easily. It requires
[PacketEvents](https://github.com/retrooper/packetevents) to function, which can be downloaded as a plugin on their
[Modrinth page](https://modrinth.com/plugin/packetevents).

1.20.3+ Features:
- Unlimited characters.
- Separate left-aligned and right-aligned text abilities.
- Fully packet based.
- Simple, lightweight, and extremely optimized.

1.13 - 1.20.2 Features:
- Unlimited characters.
- Fully packet based.
- Simple, lightweight, and extremely optimized.

1.8 - 1.12.2 Features:
- 58 characters on safe mode (equivalent line protection).
- 72 characters on danger mode (no equivalent line protection).
- Fully packet based.
- Simple, lightweight, and extremely optimized.

As of sharkbyte-scoreboard 2.0.0, there has been a ground-up rewrite of the project that ensures all versions of
Minecraft are now handled properly. While you will likely get the best experience on modern versions due to those
versions having genuinely better netcode, this project will provide you the best possible experience on each version.

# self promo

If you are using sharkbyte-scoreboard on the Spigot platform, I would highly recommend you include
[BetterReload](https://github.com/amnoah/BetterReload) compatibility.

BetterReload adds a universal reload event, replacing the traditional /reload command. This event is passed to plugins,
allowing them to handle a reload as they see fit. Your plugin could use this event to cycle through all of your
Scoreboard objects and update the lines of text on them from your configuration.

BetterReload also allows for users to individually reload plugins, removing the need for a reload command to be built
into every plugin.

# how to use

You can add sharkbyte-scoreboard to your project using [JitPack](https://jitpack.io/#amnoah/sharkbyte-scoreboard/).
Select the dependency system you're using and copy the repository/dependency settings into your project. From there,
just reload your dependencies and you should have sharkbyte-scoreboard accessible from your project.

# support

For general support, please join my [Discord server](https://discord.gg/ey9uTg3hcy).

For issues with the project, please open an issue in the issues tab.
