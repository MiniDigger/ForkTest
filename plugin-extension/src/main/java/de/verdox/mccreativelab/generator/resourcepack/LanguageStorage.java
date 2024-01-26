package de.verdox.mccreativelab.generator.resourcepack;

import com.destroystokyo.paper.ClientOption;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LanguageStorage {
    private final Set<Translatable> customTranslations = new HashSet<>();
    private final Map<String, Map<LanguageInfo, Translatable>> translationKeyMapping = new HashMap<>();
    private final CustomResourcePack customResourcePack;

    LanguageStorage(CustomResourcePack customResourcePack) {
        this.customResourcePack = customResourcePack;
    }

    public void addTranslation(Translatable translatable) {
        customTranslations.add(translatable);
        translationKeyMapping.computeIfAbsent(translatable.key(), s -> new HashMap<>()).put(translatable.languageInfo(), translatable);
    }

    public TextComponent translateToComponent(String key, LanguageInfo languageInfo, String defaultTranslation){
        return Component.text(translate(key, languageInfo, defaultTranslation));
    }

    public String translate(String key, LanguageInfo languageInfo, String defaultTranslation){
        if(!translationKeyMapping.containsKey(key))
            return defaultTranslation;
        Map<LanguageInfo, Translatable> byLanguageTranslations = translationKeyMapping.get(key);
        if(!byLanguageTranslations.containsKey(languageInfo))
            return defaultTranslation;
        return byLanguageTranslations.get(languageInfo).content();
    }

    public String translate(String key, Player player){
        String localeKey = player.getClientOption(ClientOption.LOCALE);
        LanguageInfo languageInfo = new LanguageInfo(localeKey,"","",false);
        return translate(key, languageInfo, key);
    }

    public TextComponent translateToComponent(String key, Player player){
        return Component.text(translate(key, player));
    }

    Set<Translatable> getCustomTranslations() {
        return customTranslations;
    }

    void installLanguages() {
        customTranslations
            .stream()
            .collect(Collectors.groupingBy(Translatable::languageInfo))
            .forEach((languageInfo, translations) -> {
                JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
                for (Translatable translatable : translations)
                    jsonObjectBuilder.add(translatable.key(), translatable.content());

                AssetUtil.createJsonAssetAndInstall(jsonObjectBuilder.build(), customResourcePack, new NamespacedKey("minecraft", languageInfo.identifier()), ResourcePackAssetTypes.LANG);
            });
    }
}
