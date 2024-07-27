![Image](/files/SharkByte_Logo.png)

# sharkbyte-scoreboard

This is a platform-independent system that allows scoreboards to be created easily. It requires
[PacketEvents](https://github.com/retrooper/packetevents) to function, which can be downloaded as a plugin on their
[Modrinth page](https://modrinth.com/plugin/packetevents).

Features:
- Unlimited characters on 1.18+.
- 40-character limit on 1.8 - 1.17.2.
- Simple and lightweight.
- Can hide score numbers on 1.20.3+.

NOTE: This project will primarily be maintained for 1.20.3+. Versions beneath the UpdateScore packet rewrite may not
receive support.

More information about each module can be found inside their respective folders.

# self promo

If you are using sharkbyte-scoreboard on the Spigot platform, I would highly recommend you include
[BetterReload](https://github.com/amnoah/BetterReload) compatibility.

BetterReload adds a universal reload event, replacing the traditional /reload command. This event is passed to plugins,
allowing them to handle a reload as they see fit. Your plugin could use this event to cycle through all of your
ConfigurationFile objects and call the load() function.

BetterReload also allows for users to individually reload plugins, removing the need for a reload command to be built
into every plugin.

# how to use

You can add sharkbyte-configuration to your project using [JitPack](https://jitpack.io/#amnoah/sharkbyte-scoreboard/).
Select the dependency system you're using and copy the repository/dependency settings into your project. From there,
just reload your dependencies and you should have sharkbyte-scoreboard accessible from your project.