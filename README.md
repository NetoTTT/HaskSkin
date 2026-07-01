# HaskSkin

A lightweight Minecraft plugin for **PaperSpigot 1.8.8** that lets players set custom skins via image URL or player name, with full persistence across restarts.

## Features

- Set skin from an **image URL** (PNG, 64x64 or 128x64) via [MineSkin](https://mineskin.org)
- Set skin by copying another **player's skin** directly from Mojang
- Skin persists after server restarts
- Other players see the skin change immediately (no restart needed)

## Commands

| Command | Description |
|---------|-------------|
| `/skin <url>` | Set your skin from an image URL |
| `/skin <playername>` | Copy another player's Mojang skin |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `hask.skin` | Allows use of `/skin` | `op` |

## Requirements

- PaperSpigot / Spigot **1.8.8**
- Java **8**
- Server in **offline mode** (`online-mode=false`)

## Installation

1. Drop `HaskSkin.jar` into your `/plugins` folder
2. Restart the server
3. Use `/skin <url or playername>` in-game

## How it works

Skins are fetched asynchronously and stored in `plugins/HaskSkin/skins.yml`. On each login the skin is re-applied automatically via NMS packet injection, so no external skin management plugin is required.

> **Note:** To see your own skin change without relogging, press **F5** (third-person view) — or simply relog.

## License

[MIT](LICENSE)
