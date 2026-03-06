import json

def merge_dictionaries(file_synonyma, file_cizi_slova, output_file):
    # Načtení prvního souboru
    try:
        with open(file_synonyma, 'r', encoding='utf-8') as f1:
            dict_synonyma = json.load(f1)
    except FileNotFoundError:
        print(f"Chyba: Soubor {file_synonyma} nebyl nalezen.")
        return

    # Načtení druhého souboru
    try:
        with open(file_cizi_slova, 'r', encoding='utf-8') as f2:
            dict_cizi = json.load(f2)
    except FileNotFoundError:
        print(f"Chyba: Soubor {file_cizi_slova} nebyl nalezen.")
        return
        
    merged_dict = {}
    
    # Získání všech unikátních písmen
    all_letters = set(dict_synonyma.keys()).union(set(dict_cizi.keys()))
    
    for letter in sorted(all_letters):
        merged_dict[letter] = {}
        
        words_syn = dict_synonyma.get(letter, {})
        words_cizi = dict_cizi.get(letter, {})
        
        all_words = set(words_syn.keys()).union(set(words_cizi.keys()))
        
        for word in sorted(all_words):
            meanings = []
            
            # Zpracování synonym (ošetření, zda jde o list nebo string)
            if word in words_syn:
                syn_data = words_syn[word]
                if isinstance(syn_data, list):
                    meanings.extend(syn_data)
                elif isinstance(syn_data, str):
                    meanings.append(syn_data)
                    
            # Zpracování cizích slov (ošetření, zda jde o list nebo string)
            if word in words_cizi:
                cizi_data = words_cizi[word]
                
                # Pokud je to jen text (string), uděláme z něj seznam o 1 prvku
                if isinstance(cizi_data, str):
                    cizi_list = [cizi_data]
                elif isinstance(cizi_data, list):
                    cizi_list = cizi_data
                else:
                    cizi_list = []
                    
                # Projdeme správně seznam významů (už to nebude sekat na písmena)
                for meaning in cizi_list:
                    if meaning not in meanings:
                        meanings.append(meaning)
                        
            merged_dict[letter][word] = meanings

    # Uložení výsledku do nového souboru
    with open(output_file, 'w', encoding='utf-8') as out:
        json.dump(merged_dict, out, ensure_ascii=False, indent=2)
        
    print(f"Sloučení úspěšně dokončeno! Nový soubor: {output_file}")

# Spuštění funkce (nezapomeň zde použít tvé čisté soubory z minulého kroku)
merge_dictionaries('synonyma_dictionary.json', 'cizi_slova_dictionary.json', 'slovnik_kompletni.json')
