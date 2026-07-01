package com.hask.skin.command;

import com.hask.skin.HaskSkin;
import com.hask.skin.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CmdSkin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cApenas jogadores podem usar este comando.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("\u00A7cUso: /hskin <url da imagem PNG> \u00A7eou\u00A7c /hskin <nome de player>");
            return true;
        }

        final Player p = (Player) sender;
        final String input = args[0];
        final boolean isUrl = input.startsWith("http://") || input.startsWith("https://");
        p.sendMessage("\u00A7eBuscando skin...");

        Bukkit.getScheduler().runTaskAsynchronously(HaskSkin.get(), new Runnable() {
            public void run() {
                try {
                    String[] texture = isUrl ? fetchMineSkin(input) : fetchMojangSkin(input);
                    if (texture == null) {
                        sendSync(p, isUrl
                            ? "\u00A7cFalha ao buscar skin. Verifique se a URL \u00E9 uma imagem PNG v\u00E1lida (64x64)."
                            : "\u00A7cPlayer n\u00E3o encontrado ou sem skin customizada.");
                        return;
                    }

                    final String value = texture[0];
                    final String signature = texture[1];

                    Bukkit.getScheduler().runTask(HaskSkin.get(), new Runnable() {
                        public void run() {
                            SkinManager.setSkin(p.getUniqueId(), value, signature);
                            SkinManager.applyNMS(p, value, signature);
                            p.sendMessage("\u00A7aSkin aplicada! Outros jogadores j\u00E1 v\u00EAem.");
                            p.sendMessage("\u00A7ePara ver sua pr\u00F3pria skin: pressione \u00A7fF5\u00A7e ou relog.");
                        }
                    });
                } catch (Exception e) {
                    sendSync(p, "\u00A7cErro: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    private String[] fetchMineSkin(String imageUrl) throws Exception {
        URL url = new URL("https://api.mineskin.org/generate/url");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "HaskSkin/1.0");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        String body = "{\"url\":\"" + imageUrl + "\",\"name\":\"\",\"visibility\":0}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            HaskSkin.get().getLogger().warning("[HaskSkin] MineSkin HTTP " + status);
            return null;
        }

        String json;
        try (java.util.Scanner sc = new java.util.Scanner(conn.getInputStream(), "UTF-8")) {
            sc.useDelimiter("\\A");
            json = sc.hasNext() ? sc.next() : "";
        }

        String value = extractJson(json, "value");
        String signature = extractJson(json, "signature");
        if (value == null || signature == null) {
            HaskSkin.get().getLogger().warning("[HaskSkin] MineSkin response: " + json.substring(0, Math.min(300, json.length())));
            return null;
        }
        return new String[]{value, signature};
    }

    private String[] fetchMojangSkin(String playerName) throws Exception {
        // 1. Resolve UUID
        URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        HttpURLConnection c1 = (HttpURLConnection) uuidUrl.openConnection();
        c1.setConnectTimeout(10000); c1.setReadTimeout(10000);
        if (c1.getResponseCode() != 200) return null;
        String uuidJson;
        try (java.util.Scanner sc = new java.util.Scanner(c1.getInputStream(), "UTF-8")) {
            sc.useDelimiter("\\A"); uuidJson = sc.hasNext() ? sc.next() : "";
        }
        String uuid = extractJson(uuidJson, "id");
        if (uuid == null) return null;

        // 2. Busca textures pelo UUID
        URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        HttpURLConnection c2 = (HttpURLConnection) profileUrl.openConnection();
        c2.setConnectTimeout(10000); c2.setReadTimeout(10000);
        if (c2.getResponseCode() != 200) return null;
        String profileJson;
        try (java.util.Scanner sc = new java.util.Scanner(c2.getInputStream(), "UTF-8")) {
            sc.useDelimiter("\\A"); profileJson = sc.hasNext() ? sc.next() : "";
        }
        String value = extractJson(profileJson, "value");
        String signature = extractJson(profileJson, "signature");
        if (value == null || signature == null) return null;
        return new String[]{value, signature};
    }

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        StringBuilder sb = new StringBuilder();
        while (start < json.length()) {
            char c = json.charAt(start);
            if (c == '"') break;
            if (c == '\\' && start + 1 < json.length()) {
                sb.append(json.charAt(start + 1));
                start += 2;
                continue;
            }
            sb.append(c);
            start++;
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private void sendSync(final Player p, final String msg) {
        Bukkit.getScheduler().runTask(HaskSkin.get(), new Runnable() {
            public void run() { p.sendMessage(msg); }
        });
    }
}
