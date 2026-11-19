# InteractiveChat VelocityDiscord addon

A fork of [InteractiveChat-DiscordSRV-Addon](https://github.com/LOOHP/InteractiveChat-DiscordSRV-Addon) for [VelocityDiscord (fork)](https://github.com/SkyboundLab/VelocityDiscord).

The plugin relays the `%item%`, `%inv%`, and `%ender%` chat placeholders as Discord embeds and PNG attachments.

Death, advancement, hover/click, attachments and commands are removed.

## Setup

1. Put the jar in `plugins/` on every backend server, next to InteractiveChat.
2. Put YepLib and VelocityDiscord on the Velocity proxy.
3. Start the server. The plugin writes `config.yml` on first start.

## Build

JDK 25 is required.

```
mvn -DskipTests clean package
```

The jar lands at `common/target/InteractiveChatVelocityDiscordAddon-<version>.jar`.