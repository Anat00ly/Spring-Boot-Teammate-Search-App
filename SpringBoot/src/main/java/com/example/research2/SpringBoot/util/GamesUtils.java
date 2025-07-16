package com.example.research2.SpringBoot.util;

import java.util.Arrays;
import java.util.List;

public class GamesUtils {
    // Predefined list of popular game titles
    private static final String[] GAMES = {
            "7 Days to Die",
            "Albion Online",
            "Apex Legends",
            "ARK: Survival Ascended",
            "ARK: Survival Evolved",
            "Arma 3",
            "Arena Breakout",
            "Barotrauma",
            "Black Russia",
            "Brawl Stars",
            "Brick Rigs",
            "Counter-Strike 2",
            "Counter-Strike: Global Offensive",
            "Dead by Daylight",
            "Don't Starve Together",
            "Dota 2",
            "Dying Light",
            "Elden Ring",
            "Escape from Tarkov",
            "Euro Truck Simulator 2",
            "Factorio",
            "Far Cry 6",
            "Fortnite",
            "Garry's Mod",
            "Geometry Dash",
            "Genshin Impact",
            "Grand Theft Auto V",
            "GTA V RP",
            "Hearts of Iron IV",
            "Hunt: Showdown",
            "League of Legends",
            "Lethal Company",
            "Minecraft",
            "Mindustry",
            "Mobile Legends: Bang Bang",
            "Overwatch",
            "Palworld",
            "Phasmophobia",
            "Project Zomboid",
            "PUBG: Battlegrounds",
            "PUBG Mobile",
            "Raft",
            "Roblox",
            "Rocket League",
            "Rust",
            "Scrap Mechanic",
            "Sea of Thieves",
            "Squad",
            "Stalcraft",
            "Standoff 2",
            "Stardew Valley",
            "Terraria",
            "The Forest",
            "Tom Clancy's Rainbow Six Siege",
            "Valheim",
            "Valorant",
            "War Thunder",
            "Warframe",
            "World of Warcraft"
    };

    public static List<String> getAllGames() {
        // Convert array to list and sort it
        List<String> games = Arrays.asList(GAMES);
        games.sort(String::compareToIgnoreCase);
        return games;
    }
}