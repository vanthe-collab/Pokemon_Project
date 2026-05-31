package com.example.pokedex.data.repository

import com.example.pokedex.R
import com.example.pokedex.data.model.ChainLink
import com.example.pokedex.data.model.EvolutionDetail
import com.example.pokedex.data.model.EvolutionItem
import kotlin.collections.iterator

class EvolutionMapper {
    private val repository = PokemonRepository()
    suspend fun mapToEvolutionItems(chainGoc: ChainLink): List<EvolutionItem> {
        val rawList = mutableListOf<EvolutionItem>()
        extraEvolution(chainGoc, rawList)

        // Phân làn
        val finalList = mutableListOf<EvolutionItem>()
        val groupedByLane = rawList.groupBy { it.lane }

        groupedByLane["default"]?.let { finalList.addAll(it) }
        for ((laneKey, listInLane) in groupedByLane) {
            if (laneKey != "default") finalList.addAll(listInLane)
        }

        return finalList
    }

    suspend fun extraEvolution(chain: ChainLink, resultsList: MutableList<EvolutionItem>) {
        //tên hiện tại & ảnh hiện tại
        val defaultName = chain.species.name.replaceFirstChar { it.uppercase() }
        val defaultImageId = chain.species.url.split("/").dropLast(1).last()
        if (chain.evolution_details.isEmpty()) {
            val imageURl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${defaultImageId}.png"
            //lưu giữ lại base form
            resultsList.add(EvolutionItem(defaultName, defaultImageId.toInt(),imageURl, "base form", "default"))
        } else {
            //phân loại nhóm để dễ xử lý
            val groupFormat =
                chain.evolution_details.groupBy {
                    it.base_form?.name ?: it.region?.name ?: "default"
                }

            for ((formKey, detailList) in groupFormat) {
                val laneName = if (formKey == "default") "default" else formKey.split("-").last()
                //Copy phần gốc
                var currentName = defaultName
                var currentImageId = defaultImageId

                //kiểm tra xem có vùng miền khác không
                if (formKey != "default") {
                    val baseFormObj = detailList.firstNotNullOfOrNull { it.base_form }

                    if (baseFormObj != null) {
                        val firstNameRegion =
                            baseFormObj.name.replace("-", " ").replaceFirstChar { it.uppercase() }

                        //kiểm tra xem có măc
                        val isAlreadyExists =
                            resultsList.any { it.name.equals(firstNameRegion, ignoreCase = true) }
                        if (!isAlreadyExists) {
                            val firstIdImageRegion = baseFormObj.url.split("/").dropLast(1).last()
                            val imageUrl =
                                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${firstIdImageRegion}.png"
                            //add con đầu tiên vùng miền khác vào list trước
                            resultsList.add(
                                EvolutionItem(
                                    firstNameRegion,
                                    firstIdImageRegion.toInt(),
                                    imageUrl,
                                    "base form region", laneName
                                )
                            )
                        }
                    }
                    val regionSuffix =
                        formKey.split("-").last().replaceFirstChar { it.uppercase() }
                    currentName = "$defaultName $regionSuffix"
                    //Sửa lại id của pokemon vùng miền tương ứng
                    currentImageId = getRegionalTargetId(defaultImageId, formKey)
                }
                val imageUrlRegion =
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${currentImageId}.png"

                // 3. Dịch điều kiện cho nhóm này (Quét sạch mảng detailsList của nhóm)
                val allConditions = detailList.map { translateCondition(it) }
                val condition = allConditions.distinct().joinToString(" OR\n")
                resultsList.add(
                    EvolutionItem(
                        currentName, currentImageId.toInt(),imageUrlRegion, condition, laneName
                    )
                )
            }

        }
        processSpecialForms(chain.species.url,resultsList)
        for (chainChild in chain.evolves_to) {
            extraEvolution(chainChild, resultsList)
        }
    }
    private suspend fun processSpecialForms(speciesUrl: String, resultsList: MutableList<EvolutionItem>) {
        val speciesData = repository.getPokemonSpeciesUrl(speciesUrl)

        if (speciesData.isSuccessful && speciesData.body() != null) {
            val data = speciesData.body()!!

            val keywords = listOf(
                "-mega", "-primal", "-gmax", "-origin",
                "-black", "-white",
                "-paldea-combat-breed", "-paldea-blaze-breed", "-paldea-aqua-breed",
                "-dusk", "-dawn", "-therian", "-rider", "-crowned",
                "-attack", "-defense", "-speed",
                "-sandy", "-sky", "-heat", "-wash", "-frost", "-fan","-mow","-female","-resolute",
                "-ash", "-battle-bond", "-zen","-pirouette", "-blade","-eternal", "-unbound",
                "-10-power-construct", "-50-power-construct","-complete","-midnight", "-dusk","-eternamax"
            )

            val loreDictionary = mapOf(
                "Gmax" to "Gigantamax Factor",
                "Primal" to "Primal Reversion",
                "Origin" to "Origin Form",
                "White" to "Absofusion (DNA Splicers)",
                "Black" to "Absofusion (DNA Splicers)",
                "Dusk" to "N-Solarizer",
                "Dawn" to "N-Lunarizer",
                "Paldea" to "Region Paldea",
                "Therian" to "Therian Form",
                "Rider" to "Reins of Unity",
                "Crowned" to "Rusted Artifact",
                "Attack" to "Attack Form",
                "Defense" to "Defense Form",
                "Speed" to "Speed Form",
                "Sandy" to "Sandy Form",
                "Heat" to "Heat Form",
                "Wash" to "Wash Form",
                "Frost" to "Frost Form",
                "Fan" to "Fan Form",
                "Mow" to "Mow Form",
                "Female" to "Female",
                "Resolute" to "Resolute Form",
                "Battle Bond" to "Battle Bond Ability",
                "Ash" to "Ash Form",
                "Zen" to "Zen Mode",
                "Pirouette" to "Pirouette Form",
                "Blade" to "Blade Form",
                "Eternal" to "Eternal Flower Floette",
                "Unbound" to "Unbound Form",
                "10 Power Construct" to "10% Form",
                "50 Power Construct" to "50% Form",
                "Complete" to "Complete",
                "Midnight" to "Midnight Form",
                "Dusk" to "Dusk Form",
                "Eternamax" to "Eternamax form"
            )

            val megaforms = data.varieties.filter {
                !it.is_default && keywords.any { key ->
                    it.pokemon.name.contains(key)
                }
            }

            for (mega in megaforms) {
                val megaId = mega.pokemon.url.split("/").dropLast(1).last()
                val megaName = mega.pokemon.name.replace("-", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val image = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${megaId}.png"

                val condition = loreDictionary.entries
                    .firstOrNull { megaName.contains(it.key) }?.value ?: "Mega Evolution"

                resultsList.add(
                    EvolutionItem(
                        megaName,
                        megaId.toInt(),
                        image,
                        condition,
                        lane = "mega"
                    )
                )
            }
        }
    }
    private fun translateCondition(evoluItem: EvolutionDetail): String {
        val level = evoluItem.min_level
        val day = evoluItem.time_of_day?.replaceFirstChar { it.uppercase() }
        val region = evoluItem.region?.name?.replaceFirstChar { it.uppercase() }
        val location =
            evoluItem.location?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() }
        val itemUse = evoluItem.item?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() }
        val heldItem =
            evoluItem.held_item?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() }
        val useMove =
            evoluItem.used_move?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() }
        val moveCount = evoluItem.min_move_count
        return when (evoluItem.trigger.name) {
            "level-up" -> when {
                level != null -> when {
                    !day.isNullOrEmpty() && !region.isNullOrEmpty() -> "Lv. $level, ${day}time in $region"
                    evoluItem.relative_physical_stats != null -> when (evoluItem.relative_physical_stats) {
                        0 -> "Lv. $level, Defense = Attack"
                        1 -> "Lv. $level, Defense < Attack"
                        else -> "Lv. $level, Defense > Attack"
                    }

                    !day.isNullOrEmpty() -> "Lv. $level, ${day}time"
                    evoluItem.gender != null -> if (evoluItem.gender == 1) "Lv. $level, Female" else "Lv. $level, Male"
                    !region.isNullOrEmpty() -> "Lv. $level, in $region"
                    !evoluItem.party_type?.name.isNullOrEmpty() -> "Lv. $level, ${evoluItem.party_type.name.replaceFirstChar { it.uppercase() }} type Pokémon in party"
                    evoluItem.turn_upside_down == true -> "Lv. $level, holding console upside down"
                    evoluItem.needs_overworld_rain == true -> "Lv. $level, during rain"
                    evoluItem.needs_multiplayer == true -> "Lv. $level, while in multiplayer"
                    else -> "Lv. $level"
                }

                evoluItem.min_happiness != null -> if (!day.isNullOrEmpty()) "High Friendship, ${day}time" else "High Friendship \u2764\uFE0F"
                location != null -> "Level up at $location"
                evoluItem.known_move != null -> {
                    val knownMove = evoluItem.known_move.name.replaceFirstChar { it.uppercase() }
                        .replace("-", " ")
                    if (region != null) "After $knownMove learned in $region" else "After $knownMove learned"
                }

                day != null && heldItem != null -> "Hold $heldItem, ${day}time"
                evoluItem.known_move_type != null && evoluItem.min_affection != null -> {
                    val moveType =
                        evoluItem.known_move_type.name.replaceFirstChar { it.uppercase() }
                    "After $moveType move learned \nand either ${evoluItem.min_affection} heart in Gen 6-7"
                }

                !evoluItem.party_species?.name.isNullOrEmpty() -> "With ${evoluItem.party_species.name.replaceFirstChar { it.uppercase() }} in party"
                evoluItem.min_beauty != null -> "Level up with max beauty (${evoluItem.min_beauty})"
                evoluItem.min_steps != null -> "Walk ${evoluItem.min_steps} steps in Let's Go mode"
                else -> "Happiness/Day/Night"
            }

            "use-item" -> when {
                itemUse != null && region != null -> "Use $itemUse in $region"
                itemUse != null && !day.isNullOrEmpty() -> "Use $itemUse under a $day\nin Legends: Arceus"
                itemUse != null && evoluItem.gender != null -> if (evoluItem.gender == 1) "Female, use $itemUse" else "Male, use $itemUse"
                itemUse != null -> "Use $itemUse"
                else -> "Use Item"
            }

            "trade" -> when {
                heldItem != null -> "Trade holding $heldItem"
                evoluItem.trade_species != null -> "Trade with ${evoluItem.trade_species.name.replaceFirstChar { it.uppercase() }}"
                else -> "Trade"
            }

            "use-move" -> if (useMove != null && moveCount != null) "Use $useMove $moveCount times" else " "

            "three-critical-hits" -> "Three critical hits in one battle"

            "strong-style-move" -> if (useMove != null && moveCount != null) "Use $useMove $moveCount times in Strong Style" else " "

            "agile-style-move" -> {
                val nameTrigger = evoluItem.trigger.name.replace("-", " ")
                if (!useMove.isNullOrEmpty() && moveCount != null && moveCount != 0) "Use $useMove $moveCount times\nin $nameTrigger" else " "
            }

            "shed" -> "Empty spot in party, Pokéball in bag + Ninjask"

            "recoil-damage" -> if (evoluItem.min_damage_taken != null) "Receive ${evoluItem.min_damage_taken} recoil damage\nin battle" else "Error in recoil damage"

            "take-damage" -> if (evoluItem.min_damage_taken != null) "Take ${evoluItem.min_damage_taken} damage in battle" else "Error in take damage"

            "three-defeated-bisharp" -> "${
                evoluItem.trigger.name.replace("-", " ").replaceFirstChar { it.uppercase() }
            } that are holding\nLeader's Crest"

            "spin" -> "Spin around holding Sweet"

            "tower-of-waters", "tower-of-darkness" -> "In ${
                evoluItem.trigger.name.replace("-", " ").replaceFirstChar { it.uppercase() }
            }"

            "other" -> if (level != null) "Lv. $level" else "Unknown condition (Other)"

            "gimmighoul-coins" -> "Collect 999 Coins from Roaming Form"

            else -> "Special"
        }
    }

    private fun getRegionalTargetId(defaultImageId: String, baseFormName: String): String {
        return when {
            defaultImageId == "89" && baseFormName.contains("alola") -> "10113"
            defaultImageId == "705" && baseFormName.contains("hisui") -> "10241"
            defaultImageId == "713" && baseFormName.contains("hisui") -> "10243"
            defaultImageId == "706" && baseFormName.contains("hisui") -> "10242"
            defaultImageId == "628" && baseFormName.contains("hisui") -> "10240"
            defaultImageId == "555" && baseFormName.contains("galar") -> "10177"
            defaultImageId == "549" && baseFormName.contains("hisui") -> "10237"
            defaultImageId == "571" && baseFormName.contains("hisui") -> "10239"
            defaultImageId == "51" && baseFormName.contains("alola") -> "10106"
            defaultImageId == "122" && baseFormName.contains("galar") -> "10168"        //mr-mime galar
            defaultImageId == "53" && baseFormName.contains("alola") -> "10108"         //persian alola
            defaultImageId == "20" && baseFormName.contains("alola") -> "10092"         //raticate alola
            defaultImageId == "101" && baseFormName == "voltorb-hisui" -> "10232"
            defaultImageId == "103" && baseFormName.contains("alola") -> "10114"
            defaultImageId == "110" && baseFormName.contains("galar") -> "10167"        // Weezing Galar
            defaultImageId == "105" && baseFormName.contains("alola") -> "10115"        // Marowak Alola
            defaultImageId == "26" && baseFormName.contains("alola") -> "10100"          //Raichu Alola
            defaultImageId == "75" && baseFormName.contains("alola") -> "10110"          //Gravaler - alola
            defaultImageId == "76" && baseFormName.contains("alola") -> "10111"          //Golem - alola
            defaultImageId == "78" && baseFormName.contains("galar") -> "10163"          //Rapidash - galar
            defaultImageId == "80" && baseFormName.contains("galar") -> "10165"          //slowbro - alola
            defaultImageId == "199" && baseFormName.contains("galar") -> "10172"          //slowking - alola
            defaultImageId == "28" && baseFormName.contains("alola") -> "10102"          //sandslash - alola
            defaultImageId == "38" && baseFormName.contains("alola") -> "10104"          //Ninetales - alola
            defaultImageId == "264" && baseFormName.contains("alola") -> "10175"          //Ninetales - alola
            else -> defaultImageId
        }
    }

    fun getColorId(type: String): Int {
        return when (type) {
            "fire" -> R.color.poke_fire
            "water" -> R.color.poke_water
            "grass" -> R.color.poke_grass
            "electric" -> R.color.poke_electric
            "ice" -> R.color.poke_ice
            "fighting" -> R.color.poke_fighting
            "poison" -> R.color.poke_poison
            "ground" -> R.color.poke_ground
            "flying" -> R.color.poke_flying
            "psychic" -> R.color.poke_psychic
            "rock" -> R.color.poke_rock
            "bug" -> R.color.poke_bug
            "ghost" -> R.color.poke_ghost
            "dragon" -> R.color.poke_dragon
            "dark" -> R.color.poke_dark
            "steel" -> R.color.poke_steel
            "fairy" -> R.color.poke_fairy
            else -> R.color.poke_normal
        }
    }


}