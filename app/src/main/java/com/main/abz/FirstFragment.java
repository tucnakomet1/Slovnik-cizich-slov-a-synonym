package com.main.abz;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.main.abz.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    // Sorted by alphabet using TreeMap
    private final TreeMap<String, String> dictionary = new TreeMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadDictionary();   // Load dictionary from JSON file
        pickRandomWord();   // Pick random word on a start screen

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // Search field for a word
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchWord(query.trim().toLowerCase());
                return true;
            }

            // When the text in the search field changes, we display all results.
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    // We hide the random word
                    binding.randomWordCard.setVisibility(View.GONE);
                    searchWord(newText.trim().toLowerCase());
                } else {
                    // We show the random word again
                    pickRandomWord();
                    binding.randomWordCard.setVisibility(View.VISIBLE);
                    binding.resultCard.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    // Function for choosing the random word
    private void pickRandomWord() {
        if (dictionary.isEmpty()) return;

        List<String> keys = new ArrayList<>(dictionary.keySet());
        Random random = new Random();
        String randomWord = keys.get(random.nextInt(keys.size()));
        String synonyms = dictionary.get(randomWord);

        String capitalizedWord = randomWord.substring(0, 1).toUpperCase() + randomWord.substring(1);

        binding.randomWordTextView.setText(capitalizedWord);
        binding.randomSynonymsTextView.setText(Html.fromHtml("<font color='#CCCCCC'>" + synonyms + "</font>", Html.FROM_HTML_MODE_LEGACY));
    }

    // Function for loading the dictionary from JSON file (source https://slovnik-cizich-slov.abz.cz/ and https://www.slovnik-synonym.cz/)
    private void loadDictionary() {
        try {
            // Downloaded JSON file with all the words
            InputStream is = requireContext().getAssets().open("slovnik_kompletni.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, StandardCharsets.UTF_8);

            // Reading as JSON objects
            JSONObject root = new JSONObject(jsonStr);
            Iterator<String> letters = root.keys();

            while (letters.hasNext()) {
                String letter = letters.next();
                JSONObject wordsObj = root.getJSONObject(letter);
                Iterator<String> words = wordsObj.keys();

                while (words.hasNext()) {
                    String word = words.next();
                    JSONArray synArray = wordsObj.getJSONArray(word);

                    StringBuilder synonymsBuilder = new StringBuilder();
                    for (int i = 0; i < synArray.length(); i++) {
                        synonymsBuilder.append(synArray.getString(i));
                        if (i < synArray.length() - 1) {
                            synonymsBuilder.append(", ");
                        }
                    }
                    dictionary.put(word.toLowerCase(), synonymsBuilder.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Chyba při načítání slovníku", Toast.LENGTH_SHORT).show();
        }
    }

    // Search for word in the dictionary
    private void searchWord(String query) {
        StringBuilder resultsBuilder = new StringBuilder();
        int count = 0;

        // We go through the entire dictionary and search for words BEGINNING with the specified text.
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            String word = entry.getKey();

            if (word.startsWith(query)) {
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                String synonyms = entry.getValue();

                // The word is white and bold, synonyms are light gray.
                resultsBuilder.append("<font color='#FFFFFF'><b>").append(capitalizedWord).append("</b></font><br>");
                resultsBuilder.append("<font color='#CCCCCC'>").append(synonyms).append("</font><br><br>");

                count++;

                // We limit the results to the first 30 so that the application does not crash when entering, for example, only the letter "a."
                if (count >= 30) {
                    resultsBuilder.append("<i><font color='#999999'>... zobrazeno prvních 30 výsledků</font></i>");
                    break;
                }
            }
        }

        if (count > 0) {
            // Inserting HTML code into TextView
            binding.resultsTextView.setText(Html.fromHtml(resultsBuilder.toString(), Html.FROM_HTML_MODE_LEGACY));
            binding.resultCard.setVisibility(View.VISIBLE);
        } else {
            // If nothing was found
            binding.resultCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}