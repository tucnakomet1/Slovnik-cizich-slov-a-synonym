package com.main.abz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.main.abz.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    // Sorted by alphabet using TreeMap
    private final TreeMap<String, String> dictionary = new TreeMap<>();

    // Favorite word
    private SharedPreferences prefs;
    private Set<String> favorites;
    private boolean isShowingFavorites = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load favorite words from memory
        prefs = requireActivity().getSharedPreferences("SlovnikPrefs", Context.MODE_PRIVATE);
        favorites = new HashSet<>(prefs.getStringSet("oblíbená_slova", new HashSet<>()));

        loadDictionary();   // Load dictionary from JSON file
        pickRandomWord();   // Pick random word on a start screen

        // Button to show favorite words
        binding.btnFavoritesList.setOnClickListener(v -> {
            isShowingFavorites = true;
            binding.searchView.setQuery("", false);
            binding.randomWordCard.setVisibility(View.GONE);
            showFavorites();
        });

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
                    isShowingFavorites = false;
                    binding.randomWordCard.setVisibility(View.GONE);
                    searchWord(newText.trim().toLowerCase());
                } else {
                    // We show the random word again
                    if (!isShowingFavorites) {
                        binding.resultsContainer.removeAllViews();
                        binding.resultCard.setVisibility(View.GONE);
                        pickRandomWord();
                        binding.randomWordCard.setVisibility(View.VISIBLE);
                    }
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
        binding.resultsContainer.removeAllViews(); // Delete old results
        int count = 0;

        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            String word = entry.getKey();
            if (word.startsWith(query)) {
                addWordToLayout(word, entry.getValue());
                count++;
                if (count >= 30) break; // Max 30 results
            }
        }

        binding.resultCard.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    // Show favorite words
    private void showFavorites() {
        binding.resultsContainer.removeAllViews();

        if (favorites.isEmpty()) {
            Toast.makeText(getContext(), "Zatím nemáte žádná oblíbená slova.", Toast.LENGTH_SHORT).show();
            binding.resultCard.setVisibility(View.GONE);
            return;
        }

        for (String word : favorites) {
            String synonyms = dictionary.get(word);
            if (synonyms != null) {
                addWordToLayout(word, synonyms);
            }
        }
        binding.resultCard.setVisibility(View.VISIBLE);
    }

    // Helper to add one word into layer
    private void addWordToLayout(String word, String synonyms) {
        View itemView = getLayoutInflater().inflate(R.layout.item_word, binding.resultsContainer, false);

        TextView wordTxt = itemView.findViewById(R.id.wordTextView);
        TextView synTxt = itemView.findViewById(R.id.synonymsTextView);

        android.widget.ImageView heartBtn = itemView.findViewById(R.id.heartButton);

        String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        wordTxt.setText(capitalizedWord);
        synTxt.setText(synonyms);

        // Icon
        if (favorites.contains(word)) {
            heartBtn.setImageResource(R.drawable.ic_heart_filled);
        } else {
            heartBtn.setImageResource(R.drawable.ic_heart_outline);
        }

        // When clicking at heart
        heartBtn.setOnClickListener(v -> {
            if (favorites.contains(word)) {
                favorites.remove(word);
                heartBtn.setImageResource(R.drawable.ic_heart_outline);

                // Remove word from screen
                if (isShowingFavorites) {
                    binding.resultsContainer.removeView(itemView);
                    if (favorites.isEmpty()) binding.resultCard.setVisibility(View.GONE);
                }
            } else {
                favorites.add(word);
                heartBtn.setImageResource(R.drawable.ic_heart_filled);
            }
            // Save changes
            prefs.edit().putStringSet("oblíbená_slova", new HashSet<>(favorites)).apply();
        });

        binding.resultsContainer.addView(itemView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}